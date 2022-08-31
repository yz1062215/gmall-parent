package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.lock.RedisDistLock;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/lock")
@RestController
public class LockTestController {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedisDistLock redisDistLock;
    @Autowired
    RedissonClient redissonClient;

    //ReentrantLock lock = new ReentrantLock();

    //本地锁 1W  1W
    //集群 1W  4648
    @GetMapping("/incr")//1W请求打进来
    public Result increment() {
        //lock.lock();//本地锁
        String token = redisDistLock.lock();
        //阻塞式加锁
        System.out.println("a");
        String a = redisTemplate.opsForValue().get("a");
        int i = Integer.parseInt(a);
        i++;
        redisTemplate.opsForValue().set("a", i + "");
        redisDistLock.unlock(token);
        return Result.ok();
    }

    /**
     * redisson默认锁的过期时间  30S  自动续期 没过10S自动续期
     *
     * @return
     * @throws InterruptedException
     */
    @GetMapping("/common")
    public Result redissonLock() throws InterruptedException {
        //名字相同代表同一把锁
        //1.获取锁
        RLock lock = redissonClient.getLock("lock-hello");//普通锁  可重入锁
        //2.加锁
        lock.lock();//阻塞式加锁  非要得到锁  30S过期
        //lock.lock(10, TimeUnit.SECONDS);//阻塞式加锁  非要得到锁   一旦拿到锁 10s后释放
        //boolean b = lock.tryLock();//立即尝试抢锁  抢不到一刻也不等
        //boolean b1 = lock.tryLock(10,TimeUnit.SECONDS);//10S  过期不候
        //boolean b2 = lock.tryLock(10,20,TimeUnit.SECONDS);
        //long waitTime 等待时间
        // long leaseTime 释放时间
        System.out.println("得到锁" + lock.getName().toString());
        //执行业务逻辑
        Thread.sleep(5000);
        System.out.println("执行结束");
        //3.解锁
        lock.unlock();

        return Result.ok();
    }

    /**
     * 读
     *
     * @return
     */
    int i =0;
    @GetMapping("/rw/read")
    public Result readAndWrite() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        rLock.lock();
        int x= i;
        rLock.unlock();
        return Result.ok(x);
    }


    /**
     * 写
     *
     * @return
     */
    @GetMapping("/rw/write")
    public Result Write() throws InterruptedException {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock writeLock = lock.writeLock();
        writeLock.lock();
        Thread.sleep(20000);
        i=888;
        writeLock.unlock();
        return Result.ok();
    }

    /**
     * 闭锁
     */
    @GetMapping("/getDro")
    public Result getDro(){
        RCountDownLatch latch = redissonClient.getCountDownLatch("sl-lock");

        latch.countDown();//数量减1
        return Result.ok("收集到一颗龙珠....");
    }
    @GetMapping("/xuyuan")
    public Result xuyuan() throws InterruptedException {
        RCountDownLatch slLock = redissonClient.getCountDownLatch("sl-lock");
        slLock.trySetCount(7);//设置数量
        slLock.await();//等待
        return Result.ok();
    }
}
