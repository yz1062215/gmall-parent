package com.atguigu.gmall.search.servie.impl;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.search.servie.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    GoodsRepository goodsRepository;
    @Override
    public void saveGoods(Goods goods) {
        goodsRepository.save(goods);
    }

    @Override
    public void del(Long skuId) {
        goodsRepository.deleteById(skuId);
    }
}
