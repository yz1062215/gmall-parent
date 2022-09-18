package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.order.OrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yzz
* @description 针对表【order_detail(订单明细表)】的数据库操作Service
* @createDate 2022-09-12 12:58:36
*/
public interface OrderDetailService extends IService<OrderDetail> {
    /**
     * 提交订单信息
     * @param tradeNo
     * @return
     */
    List<OrderDetail> submitOrder(Long tradeNo);

    List<OrderDetail> getOrderDetails(Long orderId, Long userId);
}
