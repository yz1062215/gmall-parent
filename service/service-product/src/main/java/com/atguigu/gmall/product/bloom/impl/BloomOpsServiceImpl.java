package com.atguigu.gmall.product.bloom.impl;

import com.atguigu.gmall.product.bloom.BloomDataQueryService;
import com.atguigu.gmall.product.bloom.BloomOpsService;
import com.atguigu.gmall.product.service.SkuInfoService;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 布隆过滤器操作类
 */
@Service
public class BloomOpsServiceImpl implements BloomOpsService {

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    SkuInfoService skuInfoService;

    @Override
    public void rebuildBloom(String bloomName, BloomDataQueryService dataQueryService) {
        //获取旧的布隆过滤器重命名
        RBloomFilter<Object> ob = redissonClient.getBloomFilter(bloomName);
        String newBloom = bloomName + "_new";
        //1.创建一个新的布隆过滤器
        RBloomFilter<Object> nb = redissonClient.getBloomFilter(newBloom);
        nb.tryInit(5000000, 0.00001);//初始化新布隆过滤器


        //List<Long> ids = skuInfoService.findAllSkuId();
        List list = dataQueryService.queryData();
        //将所有的skuId添加到新布隆过滤器中
        for (Object id : list) {
            nb.add(id);
        }
        //交换布隆过滤器key  redis单线程操作
        ob.rename("swap_bloom");//老布隆下线
        nb.rename(bloomName);

        //删除老布隆
        ob.deleteAsync();//异步删除
        //redissonClient.getBloomFilter("swap_bloom").deleteAsync();//删除中间布隆
    }
}
