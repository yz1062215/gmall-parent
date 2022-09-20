package com.atguigu.gmall.activity.biz;

import com.atguigu.gmall.common.result.ResultCodeEnum;

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
}
