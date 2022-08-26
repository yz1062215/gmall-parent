package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.SkuInfoMapper;
import com.atguigu.gmall.product.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【sku_info(库存单元表)】的数据库操作Service实现
 * @createDate 2022-08-23 13:45:28
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

    @Autowired
    SkuImageService skuImageService;

    @Autowired
    SkuAttrValueService skuAttrValueService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SpuSaleAttrService spuSaleAttrService;

    /**
     * sku大保存
     * @param info
     */
    @Transactional//开启事务
    @Override
    public void saveSkuInfo(SkuInfo info) {
        //1、sku基本信息保存到 sku_info
        save(info);
        Long skuId = info.getId();//回填使用的id
        //2、sku的图片信息保存到 sku_image
        for (SkuImage skuImage : info.getSkuImageList()) {
            skuImage.setSkuId(skuId);
        }
        skuImageService.saveBatch(info.getSkuImageList());

        //3、sku的平台属性名和值的关系保存到 sku_attr_value
        List<SkuAttrValue> attrValueList = info.getSkuAttrValueList();
        for (SkuAttrValue attrValue : attrValueList) {
            attrValue.setSkuId(skuId);
        }
        skuAttrValueService.saveBatch(attrValueList);

        //4、sku的销售属性名和值的关系保存到 sku_sale_attr_value
        List<SkuSaleAttrValue> saleAttrValueList = info.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue saleAttrValue : saleAttrValueList) {
            saleAttrValue.setSkuId(skuId);
            saleAttrValue.setSpuId(info.getSpuId());
        }
        skuSaleAttrValueService.saveBatch(saleAttrValueList);

    }
    //下架

    /**
     * 下架
     * @param skuId
     */
    @Override
    public void cancelSale(Long skuId) {
        //修改sku_info表中的is_sale
        skuInfoMapper.updateIsSale(skuId,0);

        //TODO es中删除该数据
    }
    //上架

    /**
     * 上架
     * @param skuId
     */
    @Override
    public void onSale(Long skuId) {
        skuInfoMapper.updateIsSale(skuId, 1);

        //TODO es中新增该数据
    }

    /**
     *
     * 获取sku商品详情....
     * @param skuId
     * @return
     */
    @Resource
    BaseCategory3Mapper baseCategory3Mapper;
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {

        //TODO √  1.商品(sku)所属的完整分类
        SkuDetailTo detailTo = new SkuDetailTo();
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);//获取商品详情
        //1.1根据三级分类id获取完整分类信息并封装到CategoryViewTo 中
        CategoryViewTo categoryViewTo=baseCategory3Mapper.getCategoryView(skuInfo.getCategory3Id());
        detailTo.setCategoryView(categoryViewTo);

        //TODO √  2.商品(sku)的基本信息
        detailTo.setSkuInfo(skuInfo);
        //TODO √   3.sku图片
        List<SkuImage> imageList= skuImageService.getSkuImage(skuId);
        skuInfo.setSkuImageList(imageList);

        //实时价格查询
        detailTo.setPrice(getSku101Price(skuId));
        //4.sku所属的spu的所有销售属性值   标识出当前sku到底是spu下的哪种组合  对该种组合高亮提示
        //根据spuId和skuId查询
        List<SpuSaleAttr> saleAttrList=spuSaleAttrService.getSaleAttrAndValueAndMarkSkuBySpuId(skuInfo.getSpuId(),skuId);
        detailTo.setSpuSaleAttrList(saleAttrList);
        //5.商品sku的类似推荐
        //6.商品介绍 规格参数 售后评论....
        return detailTo;
    }

    @Override
    public BigDecimal getSku101Price(Long skuId) {
        BigDecimal price=skuInfoMapper.get101Price(skuId);
        return price;
    }
}




