package com.atguigu.gmall.item;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.junit.jupiter.api.Test;

//@SpringBootTest
public class BloomFilterTest {
    @Test
    public void Test() {
        //    BloomFilter.create((from,into)->{
        //        into.putLong(Long.parseLong(from.toString()));
        //    }, 10000,0.0001);
        //}
        //创建布隆过滤器
        BloomFilter<Long> filter=BloomFilter.create(Funnels.longFunnel(), 10000,0.00001);

        //添加数据
        for (long i = 0; i < 20; i++) {
            filter.put(i);
        }

        //判定有没有
        System.out.println(filter.mightContain(1L));
        System.out.println(filter.mightContain(20L));
        System.out.println(filter.mightContain(99L));

    }
}
