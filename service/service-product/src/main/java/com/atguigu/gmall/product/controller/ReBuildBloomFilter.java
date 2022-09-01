package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.bloom.BloomDataQueryService;
import com.atguigu.gmall.product.bloom.BloomOpsService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/product")
public class ReBuildBloomFilter {
    @Autowired
    BloomOpsService bloomOpsService;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    BloomDataQueryService bloomDataQueryService;

    /**
     * 手动重建布隆过滤器
     * @return
     */
    @GetMapping("/bloom/rebuild/now")
    public Result ReBuildBloom(){
        String bloomName= SysRedisConst.BLOOM_SKUID;
        bloomOpsService.rebuildBloom(bloomName, bloomDataQueryService );
        return Result.ok();
    }
}
