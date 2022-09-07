package com.atguigu.gmall.search;

import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.vo.search.SearchParamVo;
import com.atguigu.gmall.search.servie.GoodsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

//@SpringBootTest
public class SearchTest {
    @Autowired
    GoodsService goodsService;

    @Test
    public void Test01(){

        SearchParamVo vo = new SearchParamVo();
        //vo.setCategory3Id(61L);
        vo.setKeyword("小米");
        goodsService.search(vo);
    }

    @Test
    public void Test(){
        String s = MD5.encrypt("111111");
        System.out.println("s = " + s);
    }
}
