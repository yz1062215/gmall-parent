package com.atguigu.gmall.product.service.impl;


import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yzz
 * @description 针对表【base_attr_info(属性表)】的数据库操作Service实现
 * @createDate 2022-08-23 13:45:28
 */
@Service
public class BaseAttrInfoServiceImpl extends ServiceImpl<BaseAttrInfoMapper, BaseAttrInfo> implements BaseAttrInfoService {
    @Resource
    BaseAttrInfoMapper baseAttrInfoMapper;//属性名mapper
    @Resource
    BaseAttrValueMapper baseAttrValueMapper;//属性值mapper
    @Override
    public List<BaseAttrInfo> getAttrInfoAndValueByCategoryId(Long c1Id, Long c2Id, Long c3Id) {
        //查询指定分类下的所有属性名和值
        List<BaseAttrInfo> infos =baseAttrInfoMapper.getAttrInfoAndValueByCategoryId(c1Id, c2Id, c3Id);
        return infos;
    }

    /**
     * 平台商品属性的新增保存业务
     * @param info
     */
    @Override
    public void saveAttrInfo(BaseAttrInfo info) {
        //TODO 平台属性的新增与修改
        //判断新增或者修改
        if (info.getId()==null){
            //平台属性新增操作
            addBaseAttrInfo(info);
        }else {
            updateBaseAttrInfo(info);
        }

    }

    private void updateBaseAttrInfo(BaseAttrInfo info) {
        //平台属性修改操作
        //1.改属性名信息
        baseAttrInfoMapper.updateById(info);
        //2.修改属性值
        List<Long> vids=new ArrayList<>();
        List<BaseAttrValue> valueList = info.getAttrValueList();

        //TODO 删除在新增之前！！！
        //对于前端未提交的数据要进行删除操作
        //数据库之前数据： 59 60 61 62
        //现在 59 61
        //说明 60 62已经被删除
        for (BaseAttrValue value : valueList) {
            Long vid = value.getId();
            if (vid!=null){
                vids.add(vid);//收集到所有 有属性值的id集合
            }
        }
        if (vids.size()>0) {  //如果有属性id的属性值存在 执行部分删除操作
            QueryWrapper<BaseAttrValue> deleteWrapper = new QueryWrapper<>();
            //delete * from base_attr_value where attr_id=11 and id not in(59 61)
            deleteWrapper.eq("attr_id", info.getId());
            deleteWrapper.notIn("id", vids);//不在有id属性的集合 即有id属性的不删除，删除没有id属性的
            baseAttrValueMapper.delete(deleteWrapper);
        }else {
            //全部删除
            QueryWrapper<BaseAttrValue> deleteWrapper = new QueryWrapper<>();
            //delete * from base_attr_value where attr_id=11 and id not in(59 61)
            deleteWrapper.eq("attr_id", info.getId());
            baseAttrValueMapper.delete(deleteWrapper);
        }

        for (BaseAttrValue attrValue : valueList) {

            //根据属性值id判断属性值为新增操作或者修改操作
            if(attrValue.getId()!=null){
                //修改操作
                baseAttrValueMapper.updateById(attrValue);
            }else if (attrValue.getId()==null){
                //新增操作
                attrValue.setAttrId(info.getId());//回填属性名id
                baseAttrValueMapper.insert(attrValue);
            }




        }
    }

    private void addBaseAttrInfo(BaseAttrInfo info) {
        //1.保存属性名
        baseAttrInfoMapper.insert(info);
        //获取已经保存的属性名的自增id
        Long infoId = info.getId();

        //2.保存属性值
        //2.1获取属性值集合
        List<BaseAttrValue> valueList = info.getAttrValueList();
        for (BaseAttrValue attrValue : valueList) {
            //遍历集合
            //回填属性名记录的自增id
            attrValue.setAttrId(infoId);//保存属性名id
            baseAttrValueMapper.insert(attrValue);
        }
    }
}




