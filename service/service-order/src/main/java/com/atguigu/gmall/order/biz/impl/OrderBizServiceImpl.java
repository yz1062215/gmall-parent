package com.atguigu.gmall.order.biz.impl;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.feign.product.SkuProductFeignClient;
import com.atguigu.gmall.feign.user.UserFeiClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.*;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderBizServiceImpl implements OrderBizService {
    @Autowired
    CartFeignClient cartFeignClient;
    @Autowired
    UserFeiClient userFeiClient;
    @Autowired
    SkuProductFeignClient skuProductFeignClient;
    @Autowired
    WareFeignClient wareFeignClient;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    OrderInfoService orderInfoService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    OrderDetailService orderDetailService;
    /**
     * 获取订单确认信息
     * @return
     */
    @Override
    public OrderConfirmDataVo getConfirmData() {
        OrderConfirmDataVo orderConfirmDataVo = new OrderConfirmDataVo();
        List<CartInfo> data = cartFeignClient.getChecked().getData();

        List<CartInfoVoNew> infoVos = data.stream()
                .map(cartInfo -> {
            CartInfoVoNew cartInfoVoNew = new CartInfoVoNew();
            //skuId传入
            cartInfoVoNew.setSkuId(cartInfo.getSkuId());
            cartInfoVoNew.setImgUrl(cartInfo.getImgUrl());
            cartInfoVoNew.setSkuName(cartInfo.getSkuName());

            //订单查询实时价格
            BigDecimal price = skuProductFeignClient.getSku101Price(cartInfo.getSkuId()).getData();
            cartInfoVoNew.setOrderPrice(price);
            cartInfoVoNew.setSkuNum(cartInfo.getSkuNum());

            //查询商品库存
            String hasStock = wareFeignClient.hasStock(cartInfo.getSkuId(), cartInfo.getSkuNum());
            cartInfoVoNew.setHasStock(hasStock);
            return cartInfoVoNew;
        }).collect(Collectors.toList());
        //1.获取购物车中选中的商品信息
        orderConfirmDataVo.setDetailArrList(infoVos);

        //2.获取购物车中商品数量
        Integer totalNum = infoVos.stream().map(cartInfo -> cartInfo.getSkuNum()).reduce((o1, o2) -> {
            return o1 + o2;
        }).get();
        orderConfirmDataVo.setTotalNum(totalNum);


        //3.统计购物车中商品的总价格
        BigDecimal totalAmount = infoVos.stream().map(cartInfo -> {
            return cartInfo.getOrderPrice().multiply(new BigDecimal(cartInfo.getSkuNum() + ""));
        }).reduce((o1, o2) -> {
            return o1.add(o2);
        }).get();
        orderConfirmDataVo.setTotalAmount(totalAmount);

        //4.获取用户地址列表
        orderConfirmDataVo.setUserAddressesList(userFeiClient.getUserAddress().getData());

        //5.获取交易跟踪id
        //5.1、订单的唯一追踪号，对外交易号（和第三方交互）。
        //5.2、用来防重复提交。 做防重令牌
        String tradeNo = generateTradeNo();
        orderConfirmDataVo.setTradeNo(tradeNo);

        return orderConfirmDataVo;


    }

    @Override
    public String generateTradeNo() {
        long millis = System.currentTimeMillis();
        Long userId = AuthUtils.getCurrentAuthInfo().getUserId();
        String tradeNo = millis+"_"+userId;


        //redis存一份  设置15分钟过期时间
        redisTemplate.opsForValue().set(SysRedisConst.ORDER_TEMP_TOKEN+tradeNo,"1",15, TimeUnit.MINUTES);
        return tradeNo;
    }

    @Override
    public boolean checkTradeNo(String tradeNo) {
        String lua="if redis.call(\"get\",KEYS[1]) == ARGV[1] then " +
                "    return redis.call(\"del\",KEYS[1]) " +
                "else " +
                "    return 0 " +
                "end";

        Long execute = redisTemplate.execute(new DefaultRedisScript<Long>(lua,
                Long.class), Arrays.asList(SysRedisConst.ORDER_TEMP_TOKEN + tradeNo),
                new String[] {"1"});
        if (execute>0){//执行行数大于0 说明有删除操作
            //说明令牌正确
            return true;
        }
        return false;
    }

    @Override
    public Long submitOrder(OrderSubmitVo submitVo,String tradeNo) {
        //不要相信前端传来的数据！！！！
        //1.验证订单令牌
        boolean b = checkTradeNo(tradeNo);
        if (!b) {
            //令牌校验失败
            throw  new GmallException(ResultCodeEnum.TOKEN_INVALID);
        }




        //2.验库存

        List<String> noStockSkus=new ArrayList<>();
        for (CartInfoVoNew infoVoNew : submitVo.getOrderDetailList()) {
            //判断库存
            Long skuId = infoVoNew.getSkuId();
            //远程调用ware查询库存
            String hasStock = wareFeignClient.hasStock(skuId, infoVoNew.getSkuNum());
            if (!"1".equals(hasStock)){
                //代表没有库存  将没有库存的商品名称放入集合中
                noStockSkus.add(infoVoNew.getSkuName());
            }

        }
        if (noStockSkus.size() > 0) {
            //说明有不存在库存的商品  抛出异常
            GmallException exception = new GmallException(ResultCodeEnum.ORDER_NO_STOCK);
            String skuName = noStockSkus.stream().reduce((A1, A2) -> A1 + " " + A2).get();
            throw new GmallException(ResultCodeEnum.ORDER_NO_STOCK.getMessage() + skuName, ResultCodeEnum.ORDER_NO_STOCK.getCode());
        }



        //3.验价格
        List<String> skuNames=new ArrayList<>();
        for (CartInfoVoNew infoVoNew : submitVo.getOrderDetailList()) {
            //远程查询最新价格
            Result<BigDecimal> price = skuProductFeignClient.getSku101Price(infoVoNew.getSkuId());


            if (!price.getData().equals(infoVoNew.getOrderPrice())){
                //如果价格不同
                skuNames.add(infoVoNew.getSkuName());
            }

        }

        if (skuNames.size()>0){
            String skuName = skuNames.stream().reduce((A1, A2) -> A1 + " " + A2).get();
            throw new GmallException(ResultCodeEnum.ORDER_PRICE_CHANGE.getMessage()+" "+skuName,ResultCodeEnum.ORDER_PRICE_CHANGE.getCode());
        }

        //4.保存订单信息到数据库
        Long orderId=orderInfoService.saveOrder(submitVo,tradeNo);

        //TODO 保存订单时

        //5、清除购物车中选中的商品
        cartFeignClient.deleteChecked();
        return orderId;
    }

    @Override
    public void closeOrder(Long orderId, Long userId) {
        ProcessStatus closed = ProcessStatus.CLOSED;
        List<ProcessStatus> expecteds=Arrays.asList(ProcessStatus.UNPAID,ProcessStatus.FINISHED);//期望范围
        //如果订单未支付 或者已经完成 关闭订单
        orderInfoService.changeOrderStatus(orderId,userId,closed,expecteds);
    }

    @Override
    public List<WareChildOrderVo> orderSplit(OrderWareMapVo params) {
        // 拆单先获取父订单id
        Long orderId = params.getOrderId();
        //查询父订单信息
        OrderInfo parentOrder = orderInfoService.getById(orderId);
        //查询父订单详情
        List<OrderDetail> details = orderDetailService.getOrderDetails(orderId, parentOrder.getUserId());
        parentOrder.setOrderDetailList(details);


        //库存组合
        List<WareMapItem> items = Jsons.toObj(params.getWareSkuMap(), new TypeReference<List<WareMapItem>>() {
        });

        //保存子订单信息
        List<OrderInfo> spiltOrders =items.stream().map(wareMapItem -> {
            OrderInfo orderInfo = saveChildOrderInfo(wareMapItem, parentOrder);
            return orderInfo;
        }).collect(Collectors.toList());

        //把父单状态修改为 已拆分
        orderInfoService.changeOrderStatus(parentOrder.getId(),
                parentOrder.getUserId(),
                ProcessStatus.SPLIT,
                Arrays.asList(ProcessStatus.PAID)
        );

        //4、转换为库存系统需要的数据
        return convertSpiltOrdersToWareChildOrderVo(spiltOrders);
    }

    /**
     *
     * @param spiltOrders
     * @return
     */
    private List<WareChildOrderVo> convertSpiltOrdersToWareChildOrderVo(List<OrderInfo> spiltOrders) {

        List<WareChildOrderVo> orderVos =spiltOrders.stream().map(orderInfo -> {
            WareChildOrderVo orderVo = new WareChildOrderVo();
            //封装:
            orderVo.setOrderId(orderInfo.getId());
            orderVo.setConsignee(orderInfo.getConsignee());
            orderVo.setConsigneeTel(orderInfo.getConsigneeTel());
            orderVo.setOrderComment(orderInfo.getOrderComment());
            orderVo.setOrderBody(orderInfo.getTradeBody());
            orderVo.setDeliveryAddress(orderInfo.getDeliveryAddress());
            orderVo.setPaymentWay(orderInfo.getPaymentWay());
            orderVo.setWareId(orderInfo.getWareId());

            //子订单明细 List<WareChildOrderDetailItemVo>  List<OrderDetail>
            List<WareChildOrderDetailItemVo> itemVos = orderInfo.getOrderDetailList()
                    .stream()
                    .map(orderDetail -> {
                        WareChildOrderDetailItemVo itemVo = new WareChildOrderDetailItemVo();
                        itemVo.setSkuId(orderDetail.getSkuId());
                        itemVo.setSkuNum(orderDetail.getSkuNum());
                        itemVo.setSkuName(orderDetail.getSkuName());
                        return itemVo;
                    }).collect(Collectors.toList());
            orderVo.setDetails(itemVos);
            return orderVo;

        }).collect(Collectors.toList());
        return orderVos;
    }

    /**
     * 保存子订单信息
     * @param wareMapItem
     * @param parentOrder
     * @return
     */
    private OrderInfo saveChildOrderInfo(WareMapItem wareMapItem, OrderInfo parentOrder) {
        //[{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        //获取不同仓库组合的商品id集合
        List<Long> skuIds = wareMapItem.getSkuIds();
        //子订单是在哪个仓库出库的  获取仓库id
        Long wareId = wareMapItem.getWareId();

        //子订单
        OrderInfo childOrderInfo = new OrderInfo();
        childOrderInfo.setConsignee(parentOrder.getConsignee());//子订单收货人
        childOrderInfo.setConsigneeTel(parentOrder.getConsigneeTel());//子订单联系方式  与父订单相同

        //4、获取到子订单的明细
        List<OrderDetail> childOrderDetails = parentOrder.getOrderDetailList()
                .stream()
                .filter(orderDetail -> skuIds.contains(orderDetail.getSkuId()))
                .collect(Collectors.toList());

        BigDecimal decimal = childOrderDetails.stream()
                .map(orderDetail ->
                        orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum() + "")))
                .reduce((o1, o2) -> o1.add(o2))
                .get();
        //子订单的总价
        childOrderInfo.setTotalAmount(decimal);


        childOrderInfo.setOrderStatus(parentOrder.getOrderStatus());
        childOrderInfo.setUserId(parentOrder.getUserId());
        childOrderInfo.setPaymentWay(parentOrder.getPaymentWay());
        childOrderInfo.setDeliveryAddress(parentOrder.getDeliveryAddress());
        childOrderInfo.setOrderComment(parentOrder.getOrderComment());
        //对外流水号
        childOrderInfo.setOutTradeNo(parentOrder.getOutTradeNo());
        //子订单体
        childOrderInfo.setTradeBody(childOrderDetails.get(0).getSkuName());
        childOrderInfo.setCreateTime(new Date());
        childOrderInfo.setExpireTime(parentOrder.getExpireTime());
        childOrderInfo.setProcessStatus(parentOrder.getProcessStatus());


        //每个子订单未来发货以后这个都不一样
        childOrderInfo.setTrackingNo("");
        childOrderInfo.setParentOrderId(parentOrder.getId());
        childOrderInfo.setImgUrl(childOrderDetails.get(0).getImgUrl());

        //子订单的所有明细。也要保存到数据库
        childOrderInfo.setOrderDetailList(childOrderDetails);
        childOrderInfo.setWareId("" + wareId);
        childOrderInfo.setProvinceId(0L);
        childOrderInfo.setActivityReduceAmount(new BigDecimal("0"));
        childOrderInfo.setCouponAmount(new BigDecimal("0"));
        childOrderInfo.setOriginalTotalAmount(new BigDecimal("0"));

        //根据当前负责的商品决定退货时间
        childOrderInfo.setRefundableTime(parentOrder.getRefundableTime());

        childOrderInfo.setFeightFee(parentOrder.getFeightFee());
        childOrderInfo.setOperateTime(new Date());

        //保存子订单
        orderInfoService.save(childOrderInfo);


        //保存子订单的明细
        childOrderInfo.getOrderDetailList()
                .stream()
                .forEach(orderDetail -> orderDetail.setOrderId(childOrderInfo.getId()));

        List<OrderDetail> detailList = childOrderInfo.getOrderDetailList();
        //子单明细保存完成
        orderDetailService.saveBatch(detailList);

        return childOrderInfo;
    }


}
