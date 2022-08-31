package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.cache.CacheOpsService;
import com.atguigu.gmall.item.feign.SkuDetailFeignClient;
import com.atguigu.gmall.item.service.SkuDetailService;
import com.atguigu.gmall.model.product.SkuImage;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.model.to.CategoryViewTo;
import com.atguigu.gmall.model.to.SkuDetailTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

@Service
@Slf4j
public class SkuDetailServiceImpl implements SkuDetailService {
    @Autowired
    SkuDetailFeignClient skuDetailFeignClient;
    @Autowired
    ThreadPoolExecutor executor;
    @Autowired
    StringRedisTemplate redisTemplate;
    //锁的粒度太大   300W请求  49 100W  50 100W  51 100w
    //锁的范围越大，粒度越大，系统并发性能越低 速度越慢
    //商品详情场景：锁的粒度应该设计到商品id级别 同一个商品id的查询使用一把锁
    ReentrantLock lock = new ReentrantLock();//锁的住   (本地锁 分布式项目锁不住)
    /*
        JUC锁
        本地锁都是在自己内存中的对象，每个服务器内存不同，不在同一个位置，本地锁锁不住分布式所有机器

        分布式锁:内存中的锁变成一个公共位置的锁
            去公共位置占坑

     */
    Map<Long,ReentrantLock> lockPool=new ConcurrentHashMap<>();//锁池  线程安全的  每个skuId对应的锁  解决锁粒度问题
    //未使用redis

    ////伪代码  分布式锁二阶段  解决拿到锁后炸的情况  给锁设置过期时间 自动解锁  setnx
    //
    //void redisLock_v2(){
    //    //1.加锁
    //    boolean b=setnx("lock",1);
    //    //2.判断
    //    if (b){
    //        //3.加上过期时间
    //        setexpire("lock",10);//设置10S过期时间  炸了自动解锁
    //        //存在问题  加锁和设置时间必须一起，保持原子操作
    //        //复现： setnx成功，设置过期时间时断电，没设置上过期时间
    //        //4.超长业务
    //    }else {
    //        //睡眠1s 只查缓存
    //    }
    //}
    ////分布式锁三阶段  加锁和设置过期时间原子操作 set lock 1 EX 60 NX
    //void redisLock_v3(){
    //
    //    //1.加锁+设置过期时间   实现原子操作
    //    boolean b=setnx_ex("lock",token,60);
    //    //2.判断
    //    if (b){
    //        //4.超长业务
    //
    //
    //
    //    }else {
    //        //睡眠1s 只查缓存
    //    }
    //}
    ////分布式锁四阶段  解锁过程不能解别人锁
    ///*
    //    极端情况：假设锁10S过期
    //        1.A运行9.5S业务结束 开始删锁 给redis发请求 速度慢
    //        2.redis第10S锁过期自动删除，B抢到锁，这个锁是B B已经开始执行
    //        3.B锁做事的时候，删锁命令顺着网线爬到Redis直接调用dellock,删除了这个key,导致删除B的锁错误，B锁没有了，C可能进来，此时两个人都在执行业务，
    //        没锁住！！！
    // */
    //void redisLock_v4(){
    //    //每个请求都会生成一个唯一令牌
    //    String token = UUID.randomUUID().toString();
    //    //1.加锁+设置过期时间   实现原子操作
    //    boolean b=setnx_ex("lock",1,60);
    //    //2.判断
    //    if (b){
    //        //4.超长业务
    //        //5.解锁
    //        String v=redisget("lock");
    //        if (token.equals(v)){//说明是我的锁
    //            del("lock");//可能还会删掉别人的锁   两个网络交互会发生各种意外
    //
    //        }
    //    }else {
    //        //睡眠1s 只查缓存
    //    }
    //}
    //
    ////分布式锁四阶段  获取锁值 + 对比 +删除
    //@Override
    //public SkuDetailTo getSkuDetail(Long skuId) {
    //    //不同sku使用自己id专用的锁  准备个map
    //    lockPool.put(skuId, new ReentrantLock());
    //    //1.看缓存中有无 sku:info:49
    //    String jsonStr = redisTemplate.opsForValue().get("sku:info:" + skuId);
    //    if ("x".equals(jsonStr)){
    //        //说明之前查询过 不过数据库没有该记录，为了避免再次回源 缓存了一个占位符
    //        return null;
    //    }
    //    if (StringUtils.isEmpty(jsonStr)){
    //        //2.redis无缓存
    //        //回源
    //        //缓存穿透问题  回源之前要用布隆/bitmap判断有没有
    //        // int result=getbit(49)
    //        //TODO  加锁解决击穿问题
    //        SkuDetailTo fromRoc=null;
    //
    //        //ReentrantLock lock = new ReentrantLock();//同一把锁才能锁住
    //        //lock.lock();  //要使用等锁
    //        //boolean b = lock.tryLock(1,TimeUnit.SECONDS);  //百万并发在1s内同时抢锁   等待逻辑在锁上
    //
    //
    //        //判断锁池是否存在当前skuId锁
    //        //  putIfAbsent  如果锁池存在sku锁则用之前的，不存在则创建新锁
    //        ReentrantLock lock = lockPool.putIfAbsent(skuId, new ReentrantLock());
    //
    //
    //        boolean b = lock.tryLock();//只要有人拿到锁其余请求就等待   立即尝试加锁，不用等 瞬发 等待逻辑在业务上
    //        if (b){
    //            //拿到锁
    //           fromRoc= getSkuDetailFromRpc(skuId);
    //        }else {
    //            //没抢到锁
    //            //Thread.sleep(1000);
    //            jsonStr = redisTemplate.opsForValue().get("sku:info:" + skuId);
    //            //逆转为 SkuDetailTo
    //            //return to;
    //        }
    //
    //
    //
    //        SkuDetailTo fromRpc = getSkuDetailFromRpc(skuId);
    //        //存入缓存
    //        String cacheJson="x";
    //
    //        //空值缓存+布隆过滤器(占位)
    //        if (fromRpc!=null){
    //            cacheJson=Jsons.toStr(fromRpc);
    //            redisTemplate.opsForValue().set("sku:info:"+skuId,cacheJson,7, TimeUnit.DAYS);//如果成功查询到则存入缓存中 过期时间设为七天
    //        }else {
    //            redisTemplate.opsForValue().set("sku:info:"+skuId,cacheJson,30, TimeUnit.MINUTES);//远程查询为空
    //        }
    //
    //        return fromRpc;
    //    }
    //    //3.如果缓存中存在
    //    SkuDetailTo skuDetailTo=Jsons.toObj(jsonStr,SkuDetailTo.class);
    //    return skuDetailTo;
    //}

    private SkuDetailTo getSkuDetailFromRpc(Long skuId) {
        SkuDetailTo detailTo = new SkuDetailTo();
        CountDownLatch downLatch = new CountDownLatch(6);//异步编排
        //远程调用service-product服务
        //Result<SkuDetailTo> skuDetail = skuDetailFeignClient.getSkuDetail(skuId);
        //CompletableFuture.runAsync(); 不用返回结果
        //CompletableFuture.supplyAsync(); 需要返回结果
        //v2  拆分粒度
        //1.查询基本信息
        //异步编排处理
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            Result<SkuInfo> result = skuDetailFeignClient.getSkuInfo(skuId);
            SkuInfo skuInfo = result.getData();
            //TODO  全局异常处理
            detailTo.setSkuInfo(skuInfo);
            return skuInfo;
        },executor);//返回skuInfo

        //2.查询商品图片信息
        CompletableFuture<Void> imageFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            if (skuInfo!=null){
                Result<List<SkuImage>> skuImages = skuDetailFeignClient.getSkuImages(skuId);
                skuInfo.setSkuImageList(skuImages.getData()); //空值判断
            }

        }, executor);//需要接收放回结果

        //3.查询商品实时价格   不需要前面返回结果
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            Result<BigDecimal> sku101Price = skuDetailFeignClient.getSku101Price(skuId);
            detailTo.setPrice(sku101Price.getData());
        }, executor);

        //4.查询销售属性名和值   需要前面返回结果
        CompletableFuture<Void> attrValuesFuture = skuInfoFuture.thenAcceptAsync((skuInfo) -> {
            if (skuInfo!=null){
                Result<List<SpuSaleAttr>> attrValues = skuDetailFeignClient.getsaleAttrValues(skuId, skuInfo.getSpuId());
                detailTo.setSpuSaleAttrList(attrValues.getData());
            }
        }, executor);

        //5.查询sku组合
        CompletableFuture<Void> skuValueJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if (skuInfo!=null){
                Result<String> skuValueJson = skuDetailFeignClient.getSkuValueJson(skuInfo.getSpuId());
                detailTo.setValuesSkuJson(skuValueJson.getData());
            }
        }, executor);

        //6.查询分类
        CompletableFuture<Void> catagoryFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            if (skuInfo!=null){
                Result<CategoryViewTo> viewToResult = skuDetailFeignClient.getcategory(skuInfo.getCategory3Id());
                detailTo.setCategoryView(viewToResult.getData());
            }
        }, executor);


        //全部线程执行完后
        CompletableFuture.allOf(imageFuture,priceFuture,attrValuesFuture,skuValueJsonFuture,catagoryFuture).join();


        //1.商品(sku)所属的完整分类
        //2.商品(sku)的基本信息
        //3.sku图片
        //4.sku所属的spu的所有销售属性值   标识出当前sku到底是spu下的哪种组合  对该种组合高亮提示
        //5.商品sku的类似推荐
        //6.商品介绍 规格参数 售后评论....
        return detailTo;
    }

    //分布式锁最终实现
    @Autowired
    CacheOpsService cacheOpsService;
    @Override
    public SkuDetailTo getSkuDetail(Long skuId) {//先查缓存 再查布隆
        String cacheKey= SysRedisConst.SKU_INFO_PREFIX +skuId;
        //1.先查缓存中有没有
        SkuDetailTo cacheData=cacheOpsService.getCacheData(cacheKey,SkuDetailTo.class);
        //2.判断
        if (cacheData==null){
            //3.缓存为空
            //问布隆过滤器  是否有这个商品
            boolean contain=cacheOpsService.bloomContains(skuId);
            if (contain){
                //布隆说有   可能有
                boolean lock=cacheOpsService.lock(skuId);//为当前商品加自己的分布式锁
                if (lock){
                    //获取锁成功 查询远程
                    //System.out.println("回源..............");
                    log.info("[{}]缓存未命中，布隆说有，准备回源...",+skuId);
                    SkuDetailTo fromRpc = getSkuDetailFromRpc(skuId);
                    cacheOpsService.saveData(cacheKey,fromRpc);
                    //解锁
                    cacheOpsService.unlock(skuId);
                    return fromRpc;
                }
                //获取锁失败
                try {
                    Thread.sleep(1000);//睡眠1s  查缓存
                   return cacheOpsService.getCacheData(cacheKey,SkuDetailTo.class);
                } catch (InterruptedException e) {
                }

            }
            //布隆过滤器判断没有
            log.info("[{}] 商品 布隆过滤器判断没有........存在隐藏的攻击风险" + skuId);
            return null;
        }
        //4.缓存有
        return  cacheData;

    }
}
