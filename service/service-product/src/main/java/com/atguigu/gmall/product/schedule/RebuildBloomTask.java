package com.atguigu.gmall.product.schedule;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.product.bloom.BloomDataQueryService;
import com.atguigu.gmall.product.bloom.BloomOpsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 定时重构布隆过滤器
 *
 *
 */
@Service
public class RebuildBloomTask {

    @Autowired
    BloomOpsService bloomOpsService;
    @Autowired
    BloomDataQueryService dataQueryService;
    @Scheduled(cron = "0 0 3 ? * 3")
    public void rebuild(){
        //System.out.println("定时任务启动...................");
        bloomOpsService.rebuildBloom(SysRedisConst.BLOOM_SKUID, dataQueryService);
    }

}
