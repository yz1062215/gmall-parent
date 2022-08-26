package com.atguigu.gmall.activity.service.impl;


import com.atguigu.gmall.activity.mapper.ActivityInfoMapper;
import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.enums.ActivityType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author yzz
* @description 针对表【activity_info(活动表)】的数据库操作Service实现
* @createDate 2022-08-26 09:14:06
*/
@Service
public class ActivityInfoServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo>
    implements ActivityInfoService {
    @Resource
    ActivityInfoMapper activityInfoMapper;
    @Override
    public IPage<ActivityInfo> getPage(Page<ActivityInfo> page) {
        QueryWrapper<ActivityInfo> wrapper = new QueryWrapper();
        wrapper.orderByDesc("id");
        Page<ActivityInfo> infoPage = activityInfoMapper.selectPage(page, wrapper);
        infoPage.getRecords().stream().forEach(item->{
            item.setActivityTypeString(ActivityType.getNameByType(item.getActivityType()));
        });
        return infoPage;
    }
}




