package com.atguigu.gmall.order.api;

import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.order.OrderConfirmDataVo;
import com.atguigu.gmall.order.biz.OrderBizService;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单api
 */
@RestController
@RequestMapping("/api/inner/rpc/order")
public class OrderApiController {
    @Autowired
    OrderBizService orderBizService;
    @Autowired
    OrderInfoService orderInfoService;

    //确认订单
    @GetMapping("/confirm/data")
    public Result<OrderConfirmDataVo> confirmOrderInfo() {
        OrderConfirmDataVo vo = orderBizService.getConfirmData();
        return Result.ok(vo);
    }

    /**
     * 查询某个订单信息
     *
     * @param orderId
     * @return
     */
    @GetMapping("/info/{orderId}")
    public Result<OrderInfo> getOrderInfo(@PathVariable("orderId") Long orderId) {

        OrderInfo orderInfo = orderInfoService.getOne(new LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getId, orderId)
                .eq(OrderInfo::getUserId, AuthUtils.getCurrentAuthInfo().getUserId()));
        return Result.ok(orderInfo);
    }

    /**
     * 保存秒杀单
     *
     * @param info
     * @return
     */
    @PostMapping("/seckillorder/submit")
    public Result<Long> submitSeckillOrder(@RequestBody OrderInfo info) {

        Long orderId = orderInfoService.submitSeckillOrder(info);
        return Result.ok(orderId);
    }
}
