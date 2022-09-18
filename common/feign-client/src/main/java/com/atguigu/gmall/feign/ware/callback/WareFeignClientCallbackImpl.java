package com.atguigu.gmall.feign.ware.callback;

import com.atguigu.gmall.feign.ware.WareFeignClient;
import org.springframework.stereotype.Service;

@Service
public class WareFeignClientCallbackImpl implements WareFeignClient {
    /**
     * 错误兜底方法
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public String hasStock(Long skuId, Integer num) {
        //远程调用失败后统一显示有货
        return "1";
    }
}
