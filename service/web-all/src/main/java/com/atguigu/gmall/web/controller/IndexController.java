package com.atguigu.gmall.web.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.to.CategoryTreeTo;
import com.atguigu.gmall.web.feign.CategoryFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    CategoryFeignClient categoryFeignClient;
    /**
     * 跳转首页
     * @return
     */
    @GetMapping({"/","/index"})
    public String indexPage(Model model){
        //视图解析器自动拼接
        //    prefix: classpath:/templates/
        //    suffix: .html
        //classpath:/templates/index/index.html

        //查询所有菜单
        //远程调用product服务查询所有菜单封装为一个树形结构模型..
        Result<List<CategoryTreeTo>> categoryTree = categoryFeignClient.getCategoryTree();
        //判断是否调用成功
        if (categoryTree.isOk()){
            //调用成功
            List<CategoryTreeTo> data = categoryTree.getData();//获取返回结果中的数据
            model.addAttribute("list",data);
        }


        return "index/index";//返回页面的逻辑视图名
    }
}
