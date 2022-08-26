package com.atguigu.gmall.activity.service;


import com.atguigu.gmall.model.activity.ActivityInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yzz
* @description 针对表【activity_info(活动表)】的数据库操作Service
* @createDate 2022-08-26 09:14:06
*/
public interface ActivityInfoService extends IService<ActivityInfo> {

    IPage<ActivityInfo> getPage(Page<ActivityInfo> page);
}
