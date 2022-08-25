package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.BaseSaleAttrMapper;
import com.atguigu.gmall.product.mapper.SpuImageMapper;
import com.atguigu.gmall.product.mapper.SpuInfoMapper;
import com.atguigu.gmall.product.service.SpuImageService;
import com.atguigu.gmall.product.service.SpuInfoService;
import com.atguigu.gmall.product.service.SpuSaleAttrService;
import com.atguigu.gmall.product.service.SpuSaleAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【spu_info(商品表)】的数据库操作Service实现
 * @createDate 2022-08-23 13:45:28
 */
@Service
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoMapper, SpuInfo> implements SpuInfoService {

    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id", spuInfo.getCategory3Id());
        wrapper.orderByDesc("id");
        return baseMapper.selectPage(spuInfoPage, wrapper);
    }

    @Resource
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Resource
    SpuImageMapper spuImageMapper;
    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> wrapper = new QueryWrapper<>();
        wrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(wrapper);
    }
    @Resource
    SpuInfoMapper spuInfoMapper;
    @Resource
    SpuImageService spuImageService;
    @Resource
    SpuSaleAttrValueService spuSaleAttrValueService;
    @Resource
    SpuSaleAttrService spuSaleAttrService;
    @Transactional//事务注解
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //spu_info大保存。spu_info、spu_image、spu_sale_attr、spu_sale_attr_value

        //1、把 spu基本信息保存到 spu_info表中
        spuInfoMapper.insert(spuInfo);
        Long spuInfoId = spuInfo.getId();//获取spu保存后的自增id

        //2、把 spu的图片保存到 spu_image
        List<SpuImage> imageList = spuInfo.getSpuImageList();//获取spuInfo中的图片集合
        for (SpuImage image : imageList) {
            //将spuId回填到SpuImage中
            image.setSpuId(spuInfoId);
        }
        //对图片进行批量保存 mapper中没有批量保存方法
        spuImageService.saveBatch(imageList);

        //3、保存销售属性名 到 spu_sale_attr
        List<SpuSaleAttr> attrNameList = spuInfo.getSpuSaleAttrList();
        for (SpuSaleAttr attr : attrNameList) {
            //回填spu_id
            attr.setSpuId(spuInfoId);
            //4、拿到这个销售属性名对应的所有销售属性值集合
            List<SpuSaleAttrValue> valueList = attr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue value : valueList) {
                //回填spu_id
                value.setSpuId(spuInfoId);
                //回填销售属性名
                value.setSaleAttrName(attr.getSaleAttrName());
            }
            //保存销售属性值
            spuSaleAttrValueService.saveBatch(valueList);
        }
        //保存销售属性名
        spuSaleAttrService.saveBatch(attrNameList);

    }
}




