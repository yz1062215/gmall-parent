package com.atguigu.gmall.order.biz.impl;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.feign.cart.CartFeignClient;
import com.atguigu.gmall.feign.product.SkuProductFeignClient;
import com.atguigu.gmall.feign.user.UserFeiClient;
import com.atguigu.gmall.feign.ware.WareFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.vo.order.CartInfoVoNew;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.service.OrderInfoService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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


}
