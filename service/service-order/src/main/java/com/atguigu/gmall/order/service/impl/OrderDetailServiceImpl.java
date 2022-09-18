package com.atguigu.gmall.order.service.impl;


import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.service.OrderDetailService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author yzz
* @description 针对表【order_detail(订单明细表)】的数据库操作Service实现
* @createDate 2022-09-12 12:58:36
*/
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
    implements OrderDetailService {
    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Override
    public List<OrderDetail> submitOrder(Long tradeNo) {
        return null;
    }

    /**
     * 查询父订单明细
     * @param orderId
     * @param userId
     * @return
     */
    @Override
    public List<OrderDetail> getOrderDetails(Long orderId, Long userId) {
        return orderDetailMapper.selectList(new LambdaQueryWrapper<OrderDetail>()
                .eq(OrderDetail::getUserId,userId)
                .eq(OrderDetail::getOrderId,orderId));
    }
}




