package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.annotations.LoginRequried;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.bean.enums.PaymentWay;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class OrderController {


    @Reference
    CartInfoService cartInfoService;

    @Reference
    UserInfoService userInfoService;

    @Reference
    OrderService orderService;

    @Reference
    SkuInfoService skuInfoService;

    @RequestMapping("submitOrder")
    @LoginRequried(autoRedirect = true)
    public String submitOrder(String deliveryAddressId,
                              String tradeCode, HttpServletRequest request,
                              HttpServletResponse response, ModelMap map) {

        String userId = (String) request.getAttribute("userId");

        //检查tradeCode是否过期
        boolean b = orderService.checkTradeCode(tradeCode, userId);

        if (b) {
            //获取用户的订单地址
            UserAddress userAddress = userInfoService.getAddressById(deliveryAddressId);

            //尝试从获取购物车
            List<CartInfo> cartListFromCacah = cartInfoService.getCartListFromCacah(userId);

            //封装订单对象，并保存订单 ，注意订单数据的一致性校验和，库存价格
            OrderInfo orderInfo = new OrderInfo();
            orderInfo.setUserId(userId);
            orderInfo.setConsignee(userAddress.getConsignee());
            orderInfo.setTotalAmount(getMySum(cartListFromCacah));
            orderInfo.setCreateTime(new Date());
            orderInfo.setPaymentWay(PaymentWay.ONLINE);
            orderInfo.setOrderComment("谷粒商城");
            orderInfo.setDeliveryAddress(userAddress.getUserAddress());
            orderInfo.setConsigneeTel(userAddress.getPhoneNum());
            //设置订单的有效时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            orderInfo.setExpireTime(calendar.getTime());
            //设置对外的订单号
            String outTradeNo = "atguigu" + userId;
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String format = sdf.format(date);
            outTradeNo = outTradeNo + format + System.currentTimeMillis();
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setProcessStatus("订单已提交");
            orderInfo.setOrderStatus("订单已提交");

            List<OrderDetail> orderDetails = new ArrayList<>();
            List<String> delCarts = new ArrayList<>();

            //对订单详情进行封装
            for (CartInfo cartInfo : cartListFromCacah) {
                if (cartInfo.getIsChecked().equals("1")) {
                    //进行验价
                    SkuInfo skuById = skuInfoService.getSkuById(cartInfo.getSkuId());
                    int i = skuById.getPrice().compareTo(cartInfo.getSkuPrice());
                    //价格没变
                    if (i == 0) {
                        //封装订单详情对象
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setSkuNum(cartInfo.getSkuNum());
                        orderDetail.setOrderPrice(cartInfo.getCartPrice());
                        orderDetail.setImgUrl(cartInfo.getImgUrl());
                        orderDetail.setSkuId(cartInfo.getSkuId());
                        orderDetail.setSkuName(cartInfo.getSkuName());

                        orderDetails.add(orderDetail);
                        delCarts.add(cartInfo.getId());
                    } else {
                        return "tradeFail";
                    }
                    //验库存

                }
            }
            orderInfo.setOrderDetailList(orderDetails);

            //保存订单
            orderService.savaOrder(orderInfo);

            // 删除购物车中已经保存订单数据
            // cartService.delCartByIds(delCartIds);
            return "pay";
        } else {
            return "tradeFail";
        }
    }

    @LoginRequried(autoRedirect = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request,
                          HttpServletResponse response, ModelMap map) {

        String userId = (String) request.getAttribute("userId");

        //从缓存中查询购物车的数据
        List<CartInfo> cartListFromCacah = cartInfoService.getCartListFromCacah(userId);

        //将购物车数据转化为订单列表数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartListFromCacah) {
            String isChecked = cartInfo.getIsChecked();

            if ("1".equals(isChecked)) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setHasStock("1");

                orderDetailList.add(orderDetail);
            }

        }
        //查询收货人的地址
        List<UserAddress> userAddressList = userInfoService.getAddressListByUserId(userId);

        map.put("userAddressList", userAddressList);
        map.put("orderDetailList", orderDetailList);
        map.put("totalAmount", getMySum(cartListFromCacah));

        //生成交易码，存入缓存
        String tradeCode = UUID.randomUUID().toString();
        map.put("tradeCode", tradeCode);
        orderService.genTradeCode(tradeCode, userId);

        return "trade";
    }

    private BigDecimal getMySum(List<CartInfo> cartList) {
        BigDecimal b = new BigDecimal("0");
        for (CartInfo cartInfo : cartList) {
            String isChecked = cartInfo.getIsChecked();

            if (isChecked.equals("1")) {
                b = b.add(cartInfo.getCartPrice());
            }
        }
        return b;
    }
}
