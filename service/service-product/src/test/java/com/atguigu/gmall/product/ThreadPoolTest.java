package com.atguigu.gmall.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootTest
public class ThreadPoolTest {
    @Autowired
    ThreadPoolExecutor executor;

    @Test
    public void Test01() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + ":" + UUID.randomUUID().toString());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        Thread.sleep(100000000000L);

    }
}
