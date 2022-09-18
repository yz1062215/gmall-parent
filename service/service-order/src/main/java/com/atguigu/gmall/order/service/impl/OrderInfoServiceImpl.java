package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.constant.MqConst;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.to.mq.OrderMsg;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yzz
 * @description 针对表【order_info(订单表 订单表)】的数据库操作Service实现
 * @createDate 2022-09-12 12:58:36
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Resource
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailService orderDetailService;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Transactional  //开启事务
    @Override
    public Long saveOrder(OrderSubmitVo submitVo, String tradeNo) {
        //1.准备订单数剧
        OrderInfo orderInfo=prepareOrderInfo(submitVo,tradeNo);
        //2.保存OrderInfo
        orderInfoMapper.insert(orderInfo);


        //3.保存OrderDetail
        List<OrderDetail> details = prepareOrderDetail(submitVo,orderInfo);

        //批量保存到订单详情库
        orderDetailService.saveBatch(details);

        //创建订单完成发送消息
        OrderMsg orderMsg = new OrderMsg(orderInfo.getId(),orderInfo.getUserId());
        rabbitTemplate.convertAndSend(MqConst.EXCHANGE_ORDER_EVNT,MqConst.RK_ORDER_CREATED, Jsons.toStr(orderMsg));
        //4.返回订单id
        return orderInfo.getId();//雪花算法生成的id
    }

    /**
     * 改变订单状态
     * @param orderId
     * @param userId
     * @param closed
     * @param expecteds
     */
    @Transactional
    @Override
    public void changeOrderStatus(Long orderId, Long userId, ProcessStatus closed, List<ProcessStatus> expecteds) {
        String processStatus = closed.name();
        String orderStatus = closed.getOrderStatus().name();

        orderInfoMapper.changeOrderStatus(orderId,userId,processStatus,orderStatus,expecteds);
    }

    /**
     * 根据用户id和对外流水号查询订单信息
     * @param outTradeNo
     * @param userId
     * @return
     */
    @Override
    public OrderInfo getOrderInfoByOutTradeNoAndUserId(String outTradeNo, Long userId) {
        return orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getOutTradeNo,outTradeNo));
    }

    @Override
    public OrderInfo getOrderInfoByOrderIdAndUserId(Long orderId, Long userId) {

        return orderInfoMapper.selectOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getUserId, userId)
                .eq(OrderInfo::getId,orderId));
    }

    /**
     * 保存订单详情信息
     * @param submitVo
     * @param orderInfo
     * @return
     */
    private List<OrderDetail> prepareOrderDetail(OrderSubmitVo submitVo, OrderInfo orderInfo) {
        List<OrderDetail> detailList = submitVo.getOrderDetailList().stream().map(vo -> {
            OrderDetail detail = new OrderDetail();
            //1.订单id  orderInfo中获取
            detail.setOrderId(orderInfo.getId());
            //2.商品id
            detail.setSkuId(vo.getSkuId());
            //TODO 用户id
            detail.setUserId(orderInfo.getUserId());
            //3.商品名
            detail.setSkuName(vo.getSkuName());
            //4.图片
            detail.setImgUrl(vo.getImgUrl());
            detail.setOrderPrice(vo.getOrderPrice());
            detail.setSkuNum(vo.getSkuNum());
            detail.setHasStock(vo.getHasStock());
            detail.setCreateTime(new Date());
            detail.setSplitTotalAmount(vo.getOrderPrice().multiply(new BigDecimal(vo.getSkuNum() + "")));//总金额
            detail.setSplitActivityAmount(new BigDecimal("0"));
            detail.setSplitCouponAmount(new BigDecimal("0"));
            return detail;
        }).collect(Collectors.toList());


        return detailList;

    }

    /**
     * 准备orderInfo
     * @param submitVo
     * @param tradeNo
     * @return
     */
    private OrderInfo prepareOrderInfo(OrderSubmitVo submitVo, String tradeNo) {
        OrderInfo orderInfo = new OrderInfo();

        //1.获取收件人信息
        orderInfo.setConsignee(submitVo.getConsignee());
        //2.获取收件人电话
        orderInfo.setConsigneeTel(submitVo.getConsigneeTel());
        //3.获取用户id   网关透传
        orderInfo.setUserId(AuthUtils.getCurrentAuthInfo().getUserId());
        //4.获取 支付方式 配送地址 订单备注
        orderInfo.setPaymentWay(submitVo.getPaymentWay());
        orderInfo.setDeliveryAddress(submitVo.getDeliveryAddress());
        orderInfo.setOrderComment(submitVo.getOrderComment());
        //5.对外交易号
        orderInfo.setOutTradeNo(tradeNo);
        //6.交易体
        orderInfo.setTradeBody(submitVo.getOrderDetailList().get(0).getSkuName());

        //7.订单创建时间
        orderInfo.setCreateTime(new Date());
        //8.订单过期时间  未支付45分钟后过期   订单状态变为关闭状态
        orderInfo.setExpireTime(new Date(System.currentTimeMillis()+1000* SysRedisConst.ORDER_CLOSE_TTL));
        //9.订单受理状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name()); //默认初始为未支付
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());

        //10.物流编号  发货后才有
        //orderInfo.setOutTradeNo("");
        //11.父订单id  TODO拆单................
        orderInfo.setParentOrderId(0L);
        //12.订单图片 默认取第一件商品的图片
        orderInfo.setImgUrl(submitVo.getOrderDetailList().get(0).getImgUrl());

        //13.优惠活动减去的价格

        orderInfo.setActivityReduceAmount(new BigDecimal("0"));
        orderInfo.setCouponAmount(new BigDecimal("0"));

        //14.订单商品总价计算
        BigDecimal totalAmount = submitVo.getOrderDetailList()
                .stream()
                .map(a -> a.getOrderPrice().multiply(new BigDecimal(a.getSkuNum() + "")))
                .reduce((o1, o2) -> o1.add(o2))
                .get();

        orderInfo.setTotalAmount(totalAmount);

        //15.原始单价格
        orderInfo.setOriginalTotalAmount(totalAmount);
        //运费  可退款时间...

        orderInfo.setFeightFee(new BigDecimal("0"));
        orderInfo.setRefundableTime(new Date());

        return orderInfo;
    }
}




