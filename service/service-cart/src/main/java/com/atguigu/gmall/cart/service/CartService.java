package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     * @param skuId
     * @param num
     * @return
     */
    SkuInfo addToCart(Long skuId, Integer num);

    /**
     * 决定购物车使用哪个Kye
     * @return
     */
    public String determinCartKey();

     SkuInfo addItem2Cart(Long skuId, Integer num, String cartKey);

    /**
     * skuInfo转为购物车内CartInfo
     * @param info
     * @return
     */
    CartInfo converSkuInfo2CartInfo(SkuInfo info);

    /**
     * CartInfo转为购物车内skuInfo
     * @param cartInfo
     * @return
     */
    SkuInfo converCartInfo2SkuInfo(CartInfo cartInfo);

    /**
     * 获取购物车信息
     * @param cartKey
     * @param skuId
     * @return
     */
    CartInfo getItemFromCart(String cartKey, Long skuId);

    /**
     * 删除购物车选中商品
     * @param cartKey
     */
    void deleteChecked(String cartKey);

    /**
     * 获取购物车选中的商品列表
     * @param cartKey
     * @return
     */
    public List<CartInfo> getCheckedItems(String cartKey);

    /**
     * 获取购物车列表信息
     * @param cartKey
     * @return
     */
    List<CartInfo> getCartList(String cartKey);

    /**
     * 合并临时购物车和登录购物车
     */
    void mergeUserAndTempCart();

    /**
     * 修改购物车内商品数量
     * @param skuId
     * @param num
     * @param cartKey
     */
    void updateItemNum(Long skuId, Integer num, String cartKey);

    /**
     *
     * 修改购物车内选中商品状态
     * @param skuId
     * @param status
     * @param cartKey
     */
    void updateChecked(Long skuId, Integer status, String cartKey);

    /**
     * 删除购物车内商品
     * @param skuId
     * @param cartKey
     */
    void deleteCartItem(Long skuId, String cartKey);
}
