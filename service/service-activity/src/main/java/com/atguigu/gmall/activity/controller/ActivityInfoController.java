package com.atguigu.gmall.activity.controller;


import com.atguigu.gmall.activity.service.ActivityInfoService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.activity.ActivityInfo;
import com.atguigu.gmall.model.enums.ActivityType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin/activity/activityInfo")
public class ActivityInfoController {
    @Autowired
    private ActivityInfoService activityInfoService;
    //http://192.168.6.1/admin/activity/activityInfo/1/10

    /**
     * 分页查询活动列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/{pageNum}/{pageSize}")
    public Result getActivityInfo(@PathVariable("pageNum")Long pageNum,
                                  @PathVariable("pageSize")Long pageSize){
        Page<ActivityInfo> page = new Page<>(pageNum,pageSize);
        IPage<ActivityInfo> pageModel=activityInfoService.getPage(page);
        return Result.ok(pageModel);
    }
    //http://192.168.6.1/admin/activity/activityInfo/get/8

    /**
     * 根据id获取活动信息
     * @param id
     * @return
     */
    @GetMapping("/get/{id}")
    public Result get(@PathVariable("id") Long id){
        ActivityInfo activityInfo = activityInfoService.getById(id);
        activityInfo.setActivityType(ActivityType.getNameByType(activityInfo.getActivityType()));
        return Result.ok(activityInfo);
    }

    /**
     * 新增活动
     * @param activityInfo
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody ActivityInfo activityInfo) {
        activityInfo.setCreateTime(new Date());
        activityInfoService.save(activityInfo);
        return Result.ok();
    }

    /**
     * 修改活动
     * @param activityInfo
     * @return
     */
    @PutMapping("update")
    public Result updateById(@RequestBody ActivityInfo activityInfo) {
        activityInfoService.updateById(activityInfo);
        return Result.ok();
    }

    /**
     * 删除活动
     * @param id
     * @return
     */
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable Long id) {
        activityInfoService.removeById(id);
        return Result.ok();
    }

    /**
     * 批量删除活动
     * @param idList
     * @return
     */
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        activityInfoService.removeByIds(idList);
        return Result.ok();
    }




}
