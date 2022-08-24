package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
    品牌api
 */
@RestController
@RequestMapping("/admin/product/baseTrademark")
public class BaseTrademarkController {

    @Autowired
    BaseTrademarkService baseTrademarkService;

    /**
     * 分页查询品牌列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    //http://192.168.6.1/admin/product/baseTrademark/1/10
    @GetMapping("/{pageNum}/{pageSize}")
    public Result baseTrademark(@PathVariable("pageNum")Long pageNum,
                                @PathVariable("pageSize")Long pageSize){
        Page<BaseTrademark> page = new Page<>(pageNum, pageSize);
        Page<BaseTrademark> pageResult = baseTrademarkService.page(page);
        return Result.ok(pageResult);
    }

    /**
     * 根据ID查询品牌
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public Result getBaseTrademark(@PathVariable("id")Long id){
        BaseTrademark trademark = baseTrademarkService.getById(id);
        return Result.ok(trademark);
    }

    /**
     * 根据ID修改品牌
     * @param trademark
     * @return
     */
    @PutMapping("/update")
    public Result updateBaseTrademark(@RequestBody BaseTrademark trademark){
        baseTrademarkService.updateById(trademark);
        return Result.ok();
    }

    /**
     * 保存品牌信息
     * @param trademark
     * @return
     */
    @PostMapping("/save")
    public Result saveBaseTrademark(@RequestBody BaseTrademark trademark){
        baseTrademarkService.save(trademark);
        return Result.ok();
    }

    /**
     * 根据ID删除品牌
     * @param id
     * @return
     */
    //http://192.168.6.1/admin/product/baseTrademark/remove/2
    @DeleteMapping("/remove/{id}")
    public Result removeBaseTrademark(@PathVariable("id")Long id){
        baseTrademarkService.removeById(id);
        return Result.ok();
    }

    /**
     * spu销售属性新增时查询销售属性列表
     * @return
     */
    //http://192.168.6.1/admin/product/baseTrademark/getTrademarkList
    @GetMapping("/getTrademarkList")
    public Result getTrademarkList(){
        //查询品牌集合
        List<BaseTrademark> trademarkList = baseTrademarkService.list();
        return Result.ok(trademarkList);
    }
}
