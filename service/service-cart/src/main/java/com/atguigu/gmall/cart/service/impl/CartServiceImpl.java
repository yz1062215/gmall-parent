package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.auth.AuthUtils;
import com.atguigu.gmall.common.constant.SysRedisConst;
import com.atguigu.gmall.common.execption.GmallException;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.Jsons;
import com.atguigu.gmall.feign.product.SkuProductFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.vo.user.UserAuthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    SkuProductFeignClient skuFeignClient;
    @Override
    public SkuInfo addToCart(Long skuId, Integer num) {
        //1.决定购物车使用那个key
        String cartKey=determinCartKey();

        //2.给购物车添加指定商品
        SkuInfo skuInfo=addItem2Cart(skuId,num,cartKey);

        //3. 购物车超时设置   设置自动续期功能  查看一次购物车信息自动续期三个月
        UserAuthInfo authInfo = AuthUtils.getCurrentAuthInfo();
        if (authInfo.getUserId()==null){
            //说明用户未登录操作购物车
            String tempKey = SysRedisConst.CART_KEY + authInfo.getUserTempId();
            //临时购物车有过期时间 自动续期
            redisTemplate.expire(tempKey,90, TimeUnit.DAYS);

        }
        return skuInfo;
    }



    /**
     * 决定购物车使用那个key
     * @return
     */
    @Override
    public String determinCartKey() {
        UserAuthInfo authInfo = AuthUtils.getCurrentAuthInfo();
        String cartKey= SysRedisConst.CART_KEY;
        if (authInfo.getUserId()!=null){
            //说明用户登录了
            cartKey=cartKey+""+authInfo.getUserId();
        }else {
           // 未登录  使用临时id
            cartKey=cartKey+""+authInfo.getUserTempId();

        }
        return cartKey;
    }

    /**
     * 给购物车添加指定商品
     * @param skuId
     * @param num
     * @param cartKey
     * @return
     */
    @Override
    public SkuInfo addItem2Cart(Long skuId, Integer num, String cartKey) {
        //1.获得购物车   购物车内为哈希结构

        //1.1 绑定哈希操作类
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);

        Boolean hasKey = cart.hasKey(skuId.toString());
        //2.获取当前购物车的品类信息
        Long itemsSize = cart.size();

        //3.如果之前skuId未添加过  为新增
        if (!hasKey){//不存在
            if (itemsSize+1>SysRedisConst.CART_ITEMS_LIMIT){
                //品类超出限制
                throw  new GmallException(ResultCodeEnum.CART_OVERFLOW);
            }
            //3.1远程获取商品信息
            SkuInfo info = skuFeignClient.getSkuInfo(skuId).getData();

            //3.2转为购物车中要保存的类型
            CartInfo item=converSkuInfo2CartInfo(info);
            item.setSkuNum(num);
            
            //3.2  保存到redis
            cart.put(skuId.toString(), Jsons.toStr(item));
            
            return info;

        } else {
            //4.如果之前添加过 则对之前的数量进行新增操作
            //4.1 获取实时价格
            Result<BigDecimal> priceData = skuFeignClient.getSku101Price(skuId);
            //4.2 获取原来的购物车信息
            CartInfo cartInfo = getItemFromCart(cartKey,skuId);

            //4.3 对商品进行更新操作
            cartInfo.setSkuNum(cartInfo.getSkuNum()+num);
            cartInfo.setSkuPrice(priceData.getData());
            cartInfo.setUpdateTime(new Date());

            //4.4 同步更新到购物车
            cart.put(skuId.toString(), Jsons.toStr(cartInfo));
            SkuInfo skuInfo = converCartInfo2SkuInfo(cartInfo);
            return skuInfo;

        }

    }




    /**
     * 把SkuInfo转为CartInfo
     * @param info
     * @return
     */
    @Override
    public CartInfo converSkuInfo2CartInfo(SkuInfo info) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(info.getId().toString());
        cartInfo.setSkuId(info.getId());
        cartInfo.setImgUrl(info.getSkuDefaultImg());
        cartInfo.setSkuName(info.getSkuName());
        cartInfo.setIsChecked(1);
        cartInfo.setCreateTime(new Date());
        cartInfo.setUpdateTime(new Date());
        cartInfo.setSkuPrice(info.getPrice());
        cartInfo.setCartPrice(info.getPrice());
        
        return cartInfo;
    }

    /**
     * CartInfo转为购物车内skuInfo
     * @param cartInfo
     * @return
     */
    @Override
    public SkuInfo converCartInfo2SkuInfo(CartInfo cartInfo) {
        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setSkuName(cartInfo.getSkuName());
        skuInfo.setSkuDefaultImg(cartInfo.getImgUrl());
        skuInfo.setId(cartInfo.getSkuId());

        return skuInfo;
    }

    /**
     * 获取之前购物车信息
     * @param cartKey
     * @param skuId
     * @return
     */
    @Override
    public CartInfo getItemFromCart(String cartKey, Long skuId) {
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);
        String jsonCartItem = cart.get(skuId.toString());
        return Jsons.toObj(jsonCartItem, CartInfo.class);
    }

    /**
     * 删除购物车选中的商品
     * @param cartKey
     */
    @Override
    public void deleteChecked(String cartKey) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        
        //1.获取选中的商品集合
        //流式操作  类似流水线  先将操作的对象转为流对象  然后设置其映射  finally 收集结果
        List<String> ids = getCheckedItems(cartKey)
                .stream()
                .map(cartInfo -> cartInfo.getSkuId().toString())
                .collect(Collectors.toList());
        if (ids!=null&&ids.size() > 0){
            hashOps.delete(ids.toArray());
        }

    }

    /**
     * 获取选中商品的集合
     * @param cartKey
     * @return
     */
    @Override
    public List<CartInfo> getCheckedItems(String cartKey) {
        List<CartInfo> cartList = getCartList(cartKey);
        List<CartInfo> checkedItems = cartList.stream()
                .filter(cartInfo -> cartInfo.getIsChecked() == 1)
                .collect(Collectors.toList());
        return checkedItems;
    }

    /**
     * 获取购物列表
     * @param cartKey
     * @return
     */
    @Override
    public List<CartInfo> getCartList(String cartKey) {
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);
        //流式编程 自定义
        List<CartInfo> infos = hashOps.values().stream()
                .map(str -> Jsons.toObj(str, CartInfo.class))
                .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                .collect(Collectors.toList());

        return infos;
    }

    /**
     * 合并临时购物车和登录用户购物车
     */
    @Override
    public void mergeUserAndTempCart() {
        UserAuthInfo authInfo = AuthUtils.getCurrentAuthInfo();
        //1、判断是否需要合并
        if(authInfo.getUserId()!=null && !StringUtils.isEmpty(authInfo.getUserTempId())){
            //2、可能需要合并
            //3、临时购物车有东西。合并后删除临时购物车
            String tempCartKey = SysRedisConst.CART_KEY+authInfo.getUserTempId();
            //3.1、获取临时购物车中所有商品
            List<CartInfo> tempCartList = getCartList(tempCartKey);
            if(tempCartList!=null && tempCartList.size()>0){
                //临时购物车有数据，需要合并
                String userCartKey = SysRedisConst.CART_KEY+authInfo.getUserId();
                for (CartInfo info : tempCartList) {
                    Long skuId = info.getSkuId();
                    Integer skuNum = info.getSkuNum();
                    addItem2Cart(skuId,skuNum,userCartKey);
                    //39   200
                    //3.2、合并成一个商品就删除一个
                    redisTemplate.opsForHash().delete(tempCartKey,skuId.toString());
                }

            }
        }
    }

    /**
     * 修改购物车内商品数量
     * @param skuId
     * @param num
     * @param cartKey
     */
    @Override
    public void updateItemNum(Long skuId, Integer num, String cartKey) {
        //1.获取购物车哈希操作类
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);
        //2.获取当前购物车信息
        CartInfo cartInfo = getItemFromCart(cartKey, skuId);
        cartInfo.setSkuNum(cartInfo.getSkuNum()+num);
        cartInfo.setUpdateTime(new Date());

        //3.同步到redis
        cart.put(skuId.toString(), Jsons.toStr(cartInfo));
    }

    /**
     * 改变商品选中状态
     * @param skuId
     * @param status
     * @param cartKey
     */
    @Override
    public void updateChecked(Long skuId, Integer status, String cartKey) {
        //1、拿到购物车
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(cartKey);

        //2、拿到要修改的商品
        CartInfo item = getItemFromCart(cartKey, skuId);
        item.setIsChecked(status);
        item.setUpdateTime(new Date());
        //3、保存
        hashOps.put(skuId.toString(),Jsons.toStr(item));
    }

    /**
     * 删除选中的商品信息
     * @param skuId
     * @param cartKey
     */
    @Override
    public void deleteCartItem(Long skuId, String cartKey) {
        //TODO
        BoundHashOperations<String, String, String> cart = redisTemplate.boundHashOps(cartKey);

        cart.delete(skuId.toString());
    }


}

