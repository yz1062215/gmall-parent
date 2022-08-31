package com.atguigu.gmall.product.init;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.product.service.SkuInfoService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 容器启动成功后 连上数据库 查到所有商品ID 在布隆过滤器中占位
 */
@Service
@Slf4j
public class SkuIdBloomInitService {

    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    RedissonClient redissonClient;

    @PostConstruct//当前组件对象创建成功启动
    /**
     * 项目已启动就运行
     */
    public void initSkuBloom(){
        log.info("布隆初始化正在进行...........");
        //查询出所有的商品id
        List<Long> ids=skuInfoService.findAllSkuId();
        //把所有id初始化到布隆过滤器中
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(SysRedisConst.BLOOM_SKUID);

        //数据太大可以分页

        boolean b = bloomFilter.isExists();
        if (!b){
            //尝试初始化布隆过滤器
            bloomFilter.tryInit(5000000, 0.0001);
        }

        //添加商品到布隆过滤器中
        for (Long skuId : ids) {
            bloomFilter.add(skuId);
        }
        log.info("布隆初始化完成...........总共添加了{} 条数据",ids.size());

        //判断
        //bloomFilter.contains(10L);
    }
}
