package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.to.CategoryTreeTo;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.service.BaseCategory2Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【base_category2(二级分类表)】的数据库操作Service实现
 * @createDate 2022-08-23 10:45:27
 */
@Service
public class BaseCategory2ServiceImpl extends ServiceImpl<BaseCategory2Mapper, BaseCategory2> implements BaseCategory2Service {
    @Resource
    BaseCategory2Mapper baseCategory2Mapper;
    @Override
    public List<BaseCategory2> getCategory1Child(Long c1Id) {
        //TODO  查询一级分类下的二级分类
        QueryWrapper<BaseCategory2> wrapper = new QueryWrapper<>();
        wrapper.eq("category1_id", c1Id );
        List<BaseCategory2> list = baseCategory2Mapper.selectList(wrapper);
        return list;
    }

    @Override
    public List<CategoryTreeTo> getAllCategoryWithTree() {
        return baseCategory2Mapper.getAllCategoryWithTree();
    }
}




