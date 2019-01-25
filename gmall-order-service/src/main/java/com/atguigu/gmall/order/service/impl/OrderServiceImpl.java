package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Override
    public void genTradeCode(String tradeCode, String userId) {
        String tradeCodeKey = "user:" + userId + ":tradeCode";

        Jedis jedis = redisUtil.getJedis();

        jedis.setex(tradeCodeKey, 60 * 30, tradeCode);

        jedis.close();
    }

    @Override
    public boolean checkTradeCode(String tradeCode, String userId) {
        boolean b = false;
        String tradeCodeKey = "user:" + userId + ":tradeCode";
        Jedis jedis = redisUtil.getJedis();
        String tradeCodeFromCache = jedis.get(tradeCodeKey);

        if (tradeCode.equals(tradeCodeFromCache)) {
            b = true;
            jedis.del(tradeCodeKey);
        }

        jedis.close();
        return b;
    }

    @Override
    public void savaOrder(OrderInfo orderInfo) {

        //保存订单信息
        orderInfoMapper.insertSelective(orderInfo);

        String orderInfoId = orderInfo.getId();

        //保存订单详情

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfoId);
            orderDetailMapper.insertSelective(orderDetail);

        }


    }


}
