package com.atguigu.gmall.web.feign;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.CategoryTreeTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("service-product")//声明远程调用的客户端
/*
    远程调用之前feign会从注册中心要到调用服务的真正地址
 */
public interface CategoryFeignClient {
    /**
     * 将请求json结果转成Result类型的对象  返回的数据List<CategoryTreeTo>
     * @return
     */
    @GetMapping("/api/inner/rpc/product/category/tree")
    Result<List<CategoryTreeTo>> getCategoryTree();

}
