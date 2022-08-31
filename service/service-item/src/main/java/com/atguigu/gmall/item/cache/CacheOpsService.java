package com.atguigu.gmall.item.cache;

public interface CacheOpsService {
    <T> T getCacheData(String cacheKey, Class<T> clz);

    /**
     * 布隆过滤器判断是否有这个商品
     * @param skuId
     * @return
     */
    boolean bloomContains(Long skuId);

    /**
     * 指定商品加分布式锁
     * @param skuId
     * @return
     */
    boolean lock(Long skuId);

    /**
     * 保存数据  不一定能查询到数据  把指定对象用指定的key保存到redis
     * @param cacheKey
     * @param fromRpc
     */
    void saveData(String cacheKey, Object fromRpc);

    /**
     * 解分布式锁
     * @param skuId
     */
    void unlock(Long skuId);
}
