package com.atguigu.gmall.search.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;
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

    /**
     * ES删除
     * @param skuId
     * @return
     */
    @DeleteMapping("/del/{skuId}")
    public Result delGoods(@PathVariable("skuId") Long skuId){
        goodsService.del(skuId);
        return Result.ok();
    }
    @PostMapping("/goods/search")
    Result<SearchResponseVo> search(@RequestBody SearchParamVo paramVo){
        //TODO 检索功能
        SearchResponseVo searchResponseVo=goodsService.search(paramVo);
        return Result.ok(searchResponseVo);
    }

}
