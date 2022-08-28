package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.ValueSkuJsonTo;
import com.atguigu.gmall.product.mapper.SpuSaleAttrMapper;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yzz
 * @description 针对表【spu_sale_attr(spu销售属性)】的数据库操作Service实现
 * @createDate 2022-08-23 13:45:28
 */
@Service
public class SpuSaleAttrServiceImpl extends ServiceImpl<SpuSaleAttrMapper, SpuSaleAttr> implements SpuSaleAttrService {
    @Resource
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        List<SpuSaleAttr> list = spuSaleAttrMapper.getspuSaleAttrListBySpuId(spuId);
        return list;
    }

    @Override
    public List<SpuSaleAttr> getSaleAttrAndValueAndMarkSkuBySpuId(Long spuId, Long skuId) {

        return spuSaleAttrMapper.getSaleAttrAndValueAndMarkSkuBySpuId(spuId,skuId);
    }

    @Override
    public String getAllSkuSaleValueJson(Long spuId) {
        List<ValueSkuJsonTo> valueSkuJsonTos=spuSaleAttrMapper.getAllSkuValueJson(spuId);
        //键值互换
        //{“118|120”：49，“119|121”： 50}
        Map<String,Long> map=new HashMap<>();
        for (ValueSkuJsonTo valueSkuJsonTo : valueSkuJsonTos) {
            String valueJson = valueSkuJsonTo.getValueJson();//115|117
            Long skuId = valueSkuJsonTo.getSkuId();//44
            map.put(valueJson,skuId);
        }
        //map转为json
        String str = Jsons.toStr(map);
        return str;
    }
}




