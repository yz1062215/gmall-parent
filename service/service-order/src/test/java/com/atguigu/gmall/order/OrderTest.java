package com.atguigu.gmall.order;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
public class OrderTest {

    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Test
    public void Test01(){
        OrderInfo orderInfo = orderInfoMapper.selectById(205L);
        System.out.println("orderInfo = " + orderInfo);
    }
    @Test
    public void ShardingTest(){

        OrderInfo info = new OrderInfo();
        info.setTotalAmount(new BigDecimal("1.1"));
        info.setUserId(1L);
        orderInfoMapper.insert(info);

        System.out.println("1号插入完成============================");

        OrderInfo info2 = new OrderInfo();
        info2.setTotalAmount(new BigDecimal("2.2"));
        info2.setUserId(2L);
        orderInfoMapper.insert(info2);
        System.out.println("2号插入完成============================");

    }
    @Test
    public void Test02(){
        List<OrderInfo> orderInfos = orderInfoMapper.selectList(null);
        for (OrderInfo orderInfo : orderInfos) {
            System.out.println("orderInfo = " + orderInfo);
        }
    }
}
