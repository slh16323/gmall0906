package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartInfoService {
    CartInfo exsistCart(CartInfo cartInfo);

    void savaCartInfo(CartInfo cartInfo);


    void flushCartCacah(String userId, String skuId);

    void updateCartInfo(CartInfo exsistCart);

    List<CartInfo> getCartListFromCacah(String userId);

    void merge(String userId, String listCartCookie);

    void flushCart(String userId);
}
