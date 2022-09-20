package com.atguigu.gmall.activity.api;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 秒杀
 */
@RestController
@RequestMapping("/api/inner/rpc/seckill")
public class SeckillApiController {

    @Autowired
    SeckillGoodsService seckillGoodsService;

    /**
     * 获取当天秒杀商品列表
     * @return
     */
    @GetMapping("/currentday/goods/list")
    public Result<List<SeckillGoods>> getCurrentDaySeckillGoods() {
        //获取当天参与秒杀的商品列表
        List<SeckillGoods> goods=seckillGoodsService.getCurrentDaySeckillCache();
        return Result.ok(goods);
    }

    /**
     * 获取秒杀商品详情
     * @param skuId
     * @return
     */
    @GetMapping("/getSeckillGood/item/{skuId}")
    public Result getSeckillGoodItem(@PathVariable("skuId")Long skuId){
        //item.skuDefaultImg  item.skuName item.costPrice item.stockCount
        //SeckillGoods good = seckillGoodsService.getById(skuId);
        SeckillGoods one = seckillGoodsService.getOne(new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getSkuId, skuId));
        return Result.ok(one);
    }

}
