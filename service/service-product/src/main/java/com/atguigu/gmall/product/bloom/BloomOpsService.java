package com.atguigu.gmall.product.bloom;

public interface BloomOpsService {
    /**
     * 手动重建布隆过滤器
     * @param bloomName
     */
    void rebuildBloom(String bloomName,BloomDataQueryService dataQueryService);
}
