package com.atguigu.gmall.search.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.search.servie.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inner/rpc/search")
public class SearcherApiController {
    @Autowired
    GoodsService goodsService;

    /**
     * ES新增
     * @param goods
     * @return
     */
    @PostMapping("/goods")
    public Result saveGoods(@RequestBody Goods goods){
        goodsService.saveGoods(goods);
        return Result.ok();
    }

    @DeleteMapping("/del/{skuId}")
    public Result delGoods(@PathVariable("skuId") Long skuId){
        goodsService.del(skuId);
        return Result.ok();
    }

}
