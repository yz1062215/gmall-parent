//package com.atguigu.gmall.product;
//
//import com.atguigu.gmall.model.product.BaseTrademark;
//import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
//import org.apache.shardingsphere.infra.hint.HintManager;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class shardingsphereTest {
//
//    @Autowired
//    BaseTrademarkMapper baseTrademarkMapper;
//
//    @Test
//    public void Test01(){
//        BaseTrademark baseTrademark1 = baseTrademarkMapper.selectById(4L);
//        System.out.println("baseTrademark = " + baseTrademark1);
//        //BaseTrademark baseTrademark2 = baseTrademarkMapper.selectById(4L);
//        //System.out.println("baseTrademark = " + baseTrademark2);
//        //BaseTrademark baseTrademark3 = baseTrademarkMapper.selectById(4L);
//        //System.out.println("baseTrademark = " + baseTrademark3);
//        //BaseTrademark baseTrademark4 = baseTrademarkMapper.selectById(4L);
//        //System.out.println("baseTrademark = " + baseTrademark4);
//        baseTrademark1.setTmName("小米新1");
//        baseTrademarkMapper.updateById(baseTrademark1);
//        System.out.println(baseTrademarkMapper.selectById(4L));
//
//        //强制走主库
//        HintManager.getInstance().setWriteRouteOnly();//强制走主库
//
//        System.out.println("更新后的:"+baseTrademarkMapper.selectById(4L));
//
//    }
//}