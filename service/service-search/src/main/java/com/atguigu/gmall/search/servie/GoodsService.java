package com.atguigu.gmall.search.servie;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.model.vo.search.SearchResponseVo;

public interface GoodsService {
    void saveGoods(Goods goods);

    void del(Long skuId);

    /**
     * 条件检索
     * @param paramVo
     * @return
     */
    SearchResponseVo search(SearchParamVo paramVo);
}
