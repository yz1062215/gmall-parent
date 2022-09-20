package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.biz.SeckillBizService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@RestController
@RequestMapping("/api/activity/seckill/auth")
public class SeckillCOntroller {
    @Autowired
    SeckillBizService bizService;

    //http://api.gmall.com/api/activity/seckill/auth/getSeckillSkuIdStr/46

    /**
     * 生成秒杀码  用于隐藏秒杀地址
     *
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillSkuIdStr/{skuId}")
    public Result getSeckillSkuStr(@PathVariable("skuId") Long skuId) {
        //生成秒杀码
        String code = bizService.generateSkuSeckillCode(skuId);
        return Result.ok(code);
    }

    //http://api.gmall.com/api/activity/seckill/auth/seckillOrder/46?skuIdStr=71727232a41e7808b8cb03496a299ff1

    /**
     * 秒杀单预下单
     *
     * @param skuId
     * @param skuIdStr
     * @return
     */
    @PostMapping("/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable("skuId") Long skuId, @RequestParam("skuIdStr") String skuIdStr) {
        ResultCodeEnum codeEnum = bizService.seckillOrder(skuId, skuIdStr);
        return Result.build("", codeEnum);
    }

    /**
     * 检查订单状态
     *
     * @param skuId
     * @return
     */
    @GetMapping("/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable("skuId") Long skuId) {
        ResultCodeEnum codeEnum = bizService.checkSeckillOrderStatus(skuId);
        return Result.build("", codeEnum);
    }

}
