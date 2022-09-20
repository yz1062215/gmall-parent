package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.feign.seckill.SeckillFeignClient;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillFeignClient seckillFeignClient;

    /**
     * 当天秒杀商品展示页面
     *
     * @param model
     * @return
     */
    @GetMapping("/seckill.html")
    public String seckillPage(Model model) {

        Result<List<SeckillGoods>> result = seckillFeignClient.getCurrentDaySeckillGoods();
        model.addAttribute("list", result.getData());
        return "seckill/index";
    }

    /**
     * 查看秒杀商品详情
     *
     * @param skuId
     * @param model
     * @return
     */
    @GetMapping("/seckill/{skuId}.html")
    public String seckillGoodsItemPage(@PathVariable("skuId") Long skuId, Model model) {
        //item.skuDefaultImg  item.skuName item.costPrice item.stockCount
        Result goodItem = seckillFeignClient.getSeckillGoodItem(skuId);
        model.addAttribute("item", goodItem.getData());
        return "seckill/item";
    }

    @GetMapping("/seckill/queue.html")
    public String SeckillQueue(@RequestParam("skuId") Long skuId, @RequestParam("skuIdStr") String skuIdStr, Model model) {
        model.addAttribute("skuId", skuId);
        model.addAttribute("skuIdStr", skuIdStr);
        return "seckill/queue";

    }
}
