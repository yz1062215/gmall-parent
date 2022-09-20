package com.atguigu.gmall.feign.seckill;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("service-activity")
@RequestMapping("/api/inner/rpc/seckill")
public interface SeckillFeignClient {


    @GetMapping("/currentday/goods/list")
     Result<List<SeckillGoods>> getCurrentDaySeckillGoods();

    @GetMapping("/getSeckillGood/item/{skuId}")
     Result getSeckillGoodItem(@PathVariable("skuId")Long skuId);
}
