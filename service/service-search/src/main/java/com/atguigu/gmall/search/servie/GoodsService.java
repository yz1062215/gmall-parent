package com.atguigu.gmall.search.servie;

import com.atguigu.gmall.model.list.Goods;

public interface GoodsService {
    void saveGoods(Goods goods);

    void del(Long skuId);
}
