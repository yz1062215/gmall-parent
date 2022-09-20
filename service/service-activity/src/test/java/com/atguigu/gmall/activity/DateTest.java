package com.atguigu.gmall.activity;

import com.atguigu.gmall.common.util.DateUtil;
import org.junit.Test;

import java.util.Date;

public class DateTest {

    @Test
    public void Test(){
        System.out.println(DateUtil.formatDate(new Date()));
    }
}
