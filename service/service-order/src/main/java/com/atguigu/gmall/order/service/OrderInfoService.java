package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderSubmitVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yzz
* @description 针对表【order_info(订单表 订单表)】的数据库操作Service
* @createDate 2022-09-12 12:58:36
*/
public interface OrderInfoService extends IService<OrderInfo> {
    /**
     * 保存订单信息到数据库
     * @param submitVo
     * @return
     */
    Long saveOrder(OrderSubmitVo submitVo,String tradeNo);


    void changeOrderStatus(Long orderId, Long userId, ProcessStatus closed, List<ProcessStatus> expecteds);
}
