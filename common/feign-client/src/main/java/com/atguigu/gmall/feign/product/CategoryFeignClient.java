package com.atguigu.gmall.feign.product;

import com.atguigu.gmall.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

//告诉SpringBoot 这是一个远程调用的客户端，调用 service-product 微服务的功能
//远程调用之前feign会自己找nacos要到 service-product 真的地址

@RequestMapping("/api/inner/rpc/product")
@FeignClient("service-product")
//service-product：当前客户端的名字，也是这个feign要发起远程调用时找的微服务的名字
public interface CategoryFeignClient {

    /**
     *1、 给 service-product 发送一个 GET方式的请求 路径是 /api/inner/rpc/product/category/tree
     *2、 拿到远程的响应 json 结果后转成 Result类型的对象，并且 返回的数据是 List<CategoryTreeTo>
     * @return
     */
//    @GetMapping("/api/inner/rpc/product/category/tree")
//    Result<List<CategoryTreeTo>> getCategoryTree();

    @GetMapping("/category/tree")
    public Result getAllCategoryWithTree();

}
