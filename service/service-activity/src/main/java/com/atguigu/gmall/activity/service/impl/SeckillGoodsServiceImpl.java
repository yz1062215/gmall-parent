package com.atguigu.gmall.activity.service.impl;


import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsCacheOpsService;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【seckill_goods】的数据库操作Service实现
 * @createDate 2022-08-26 09:14:06
 */
@Service
public class SeckillGoodsServiceImpl extends ServiceImpl<SeckillGoodsMapper, SeckillGoods> implements SeckillGoodsService {

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    SeckillGoodsCacheOpsService cacheOpsService;

    @Override
    public List<SeckillGoods> getCurrentDaySeckillGoodsList() {
        String date = DateUtil.formatDate(new Date());
        return seckillGoodsMapper.getSeckillGoodsByDate(date);
    }

    @Override
    public List<SeckillGoods> getCurrentDaySeckillCache() {

        return cacheOpsService.getSeckillGoodsFromLocal();
    }

    @Override
    public void deduceSeckillGoods(Long skuId) {
        seckillGoodsMapper.updateStockCount(skuId);
    }
}




