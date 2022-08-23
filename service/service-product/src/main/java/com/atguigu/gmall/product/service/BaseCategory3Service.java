package com.atguigu.gmall.product.service;


import com.atguigu.gmall.model.product.BaseCategory3;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yzz
* @description 针对表【base_category3(三级分类表)】的数据库操作Service
* @createDate 2022-08-23 11:05:29
*/
public interface BaseCategory3Service extends IService<BaseCategory3> {
    /**
     * 查询二级分类ID下的所有三级分类
     * @param c2Id
     * @return
     */
    List<BaseCategory3> getCategory2Child(Long c2Id);
}
