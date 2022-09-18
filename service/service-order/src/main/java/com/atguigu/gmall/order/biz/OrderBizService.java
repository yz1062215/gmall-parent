package com.atguigu.gmall.order.biz;

import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.atguigu.gmall.model.vo.order.OrderWareMapVo;
import com.atguigu.gmall.model.vo.order.WareChildOrderVo;

import java.util.List;

public interface OrderBizService {
    /**
     * 获取订单页确认的数据
     * @return
     */
    OrderConfirmDataVo getConfirmData();

    /**
     * 生成订单追踪id
     * @return
     */
    public String generateTradeNo();

    /**
     * 检查订单令牌
     * @return
     */
    public boolean checkTradeNo(String tradeNo);

    /**
     * 提交订单
     * @param orderSubmitVo
     * @return
     */
    Long submitOrder(OrderSubmitVo orderSubmitVo,String tradeNo);

    /**
     * 关闭订单
     * @param orderId
     * @param userId
     */
    void closeOrder(Long orderId, Long userId);

    /**
     * 拆单
     * @param params
     * @return
     */
    List<WareChildOrderVo> orderSplit(OrderWareMapVo params);
}
