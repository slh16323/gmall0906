package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;

public interface OrderService {
    void genTradeCode(String tradeCode, String userId);


    boolean checkTradeCode(String tradeCode, String userId);

    void savaOrder(OrderInfo orderInfo);
}
