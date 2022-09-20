package com.atguigu.gmall.activity.biz;

import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.vo.seckill.SeckillOrderConfirmVo;

public interface SeckillBizService {
    /**
     * 生成秒杀码来隐藏秒杀地址
     *
     * @param skuId
     * @return
     */
    String generateSkuSeckillCode(Long skuId);

    /**
     * 校验秒杀码是否合法
     *
     * @param skuId
     * @param code
     * @return
     */
    boolean checkSkuSeckillCode(Long skuId, String code);

    /**
     * 秒杀预排队
     *
     * @param skuId
     * @param skuIdStr
     * @return
     */
    ResultCodeEnum seckillOrder(Long skuId, String skuIdStr);

    /**
     * 每隔三秒检查秒杀订单的状态
     *
     * @param skuId
     * @return
     */
    ResultCodeEnum checkSeckillOrderStatus(Long skuId);

    /**
     * 获取秒杀页确认数据
     *
     * @param skuId
     * @return
     */
    SeckillOrderConfirmVo getSeckillOrderConfirmVo(Long skuId);

    /**
     * 提交秒杀单信息
     *
     * @param orderInfo
     * @return
     */
    Long submitSeckillOrder(OrderInfo orderInfo);
}
