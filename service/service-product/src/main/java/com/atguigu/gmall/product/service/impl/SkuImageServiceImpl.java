package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.product.mapper.SkuImageMapper;
import com.atguigu.gmall.product.service.SkuImageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【sku_image(库存单元图片表)】的数据库操作Service实现
 * @createDate 2022-08-23 13:45:28
 */
@Service
public class SkuImageServiceImpl extends ServiceImpl<SkuImageMapper, SkuImage> implements SkuImageService {
    @Resource
    SkuImageMapper skuImageMapper;

    @Override
    public List<SkuImage> getSkuImage(Long skuId) {
        QueryWrapper<SkuImage> imageWrapper = new QueryWrapper<>();
        imageWrapper.eq("sku_id", skuId);

        return skuImageMapper.selectList(imageWrapper);
    }


}




