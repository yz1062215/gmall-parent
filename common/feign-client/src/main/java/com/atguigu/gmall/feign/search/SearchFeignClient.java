package com.atguigu.gmall.feign.search;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient("service-search")
@RequestMapping("/api/inner/rpc/search")
public interface SearchFeignClient {
     //将调用者传过来的数据转为Json放入请求体中传给被调用Controller，Controller将请求体中的Json转为Goods
     @PostMapping("/goods")
     public Result saveGoods(@RequestBody Goods goods);

     @DeleteMapping("/del/{skuId}")
     public Result delGoods(@PathVariable("skuId") Long skuId);

     @PostMapping("/goods/search")
     Result<SearchResponseVo> search(@RequestBody SearchParamVo paramVo);


     @GetMapping("/goods/hotscore/{skuId}")
      Result updateHotScore(@PathVariable("skuId") Long skuId,
                                  @RequestParam("score") Long score);
}
