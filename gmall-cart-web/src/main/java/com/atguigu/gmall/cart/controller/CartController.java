package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.annotations.LoginRequried;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Reference
    SkuInfoService skuInfoService;

    @Reference
    CartInfoService cartInfoService;



    @LoginRequried(autoRedirect = false)
    @RequestMapping("addCartNum")
    public String addCartNum(CartInfo cartInfo){

        return "cartListInner";
    }

    @LoginRequried(autoRedirect = false)
    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request,
                            HttpServletResponse response,
                            CartInfo cartInfo, ModelMap map) {
        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfos = new ArrayList<>();

        if (StringUtils.isNoneBlank(userId)) {
            //从缓存中获取购物车信息
            cartInfos = cartInfoService.getCartListFromCacah(userId);
        } else {
            //从cookie中获取购物车信息
            String cartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            cartInfos = JSON.parseArray(cartCookie, CartInfo.class);
        }
        //修改购物车中商品的选中状态
        for (CartInfo info : cartInfos) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                info.setIsChecked(cartInfo.getIsChecked());
                if (StringUtils.isNoneBlank(userId)) {
                    String skuId = cartInfo.getSkuId();
                    //更新数据库
                    cartInfoService.updateCartInfo(info);
                    //刷新缓存
                    cartInfoService.flushCartCacah(userId, skuId);
                } else {
                    //覆盖cookie
                    CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos), 60 * 60 * 24, true);
                }
            }
        }


        map.put("cartList", cartInfos);
        BigDecimal totalPrice = getTotalSum(cartInfos);
        map.put("totalPrice", totalPrice);
        return "cartListInner";
    }

    @LoginRequried(autoRedirect = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap map) {

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartInfos = new ArrayList<>();

        if (StringUtils.isNoneBlank(userId)) {
            //从Redis缓存中获取数据
            cartInfos = cartInfoService.getCartListFromCacah(userId);
        } else {
            //从cookie中获取数据
            String cartListStr = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if (StringUtils.isNoneBlank(cartListStr)) {
                cartInfos = JSON.parseArray(cartListStr, CartInfo.class);
            }

        }
        //计算被选中商品的总价格
        BigDecimal totalPrice = getTotalSum(cartInfos);

        map.put("cartList", cartInfos);
        map.put("totalPrice", totalPrice);
        return "cartList";
    }

    @LoginRequried(autoRedirect = false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request,
                            HttpServletResponse response,
                            String skuId, int num,
                            ModelMap map) {
        //获取用户id
        String userId = (String) request.getAttribute("userId");

        //根据skuId查询商品的信息
        SkuInfo skuInfo = skuInfoService.getSkuById(skuId);
        //创造购物车对象对象
        CartInfo cartInfo = new CartInfo();
        //初始化购物车
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuNum(num);
        cartInfo.setIsChecked("1");
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));

        if (StringUtils.isNoneBlank(userId)) {
            cartInfo.setUserId(userId);
        }

        List<CartInfo> cartInfos = new ArrayList<>();

        if (userId != null && userId.length() > 0) {
            //用户登录
            CartInfo cart = new CartInfo();
            cart.setUserId(userId);
            cart.setSkuId(skuId);
            //查询用户的购物车中是否存在该商品
            CartInfo exsistCart = cartInfoService.exsistCart(cart);

            //判断
            if (exsistCart != null) {
                //商品存在，更新商品价格和数量
                exsistCart.setSkuNum(exsistCart.getSkuNum() + num);
                num = exsistCart.getSkuNum();
                exsistCart.setCartPrice(exsistCart.getSkuPrice().multiply(new BigDecimal(exsistCart.getSkuNum())));
                //更新数据库
                cartInfoService.updateCartInfo(exsistCart);
            } else {
                //不能存在，直接添加
                cartInfoService.savaCartInfo(cartInfo);
            }

            //刷新缓存
            cartInfoService.flushCart(userId);

        } else {
            //用户没有登录
            //从cookie中获取购物车对象
            String listCartCookieStr = CookieUtil.getCookieValue(request, "listCartCookie", true);

            cartInfos = JSON.parseArray(listCartCookieStr, CartInfo.class);

            //判断cookie中存不存在购物车对象
            if (StringUtils.isBlank(listCartCookieStr)) {
                cartInfos = new ArrayList<>();
                //直接添加到购物车中
                cartInfos.add(cartInfo);
            } else {
                //判断购物车中是否存在相同的商品
                boolean b = if_new_cart(cartInfos, cartInfo);

                if (b) {
                    //不存在相同的cart
                    cartInfos.add(cartInfo);
                } else {
                    //存在相同的商品
                    for (CartInfo info : cartInfos) {
                        if (skuId.equals(info.getSkuId())) {
                            //更新商品的数量和总价格
                            info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                            num = info.getSkuNum();
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            }
            //覆盖浏览器的cookie
            CookieUtil.setCookie(request, response, "listCartCookie", JSON.toJSONString(cartInfos), 1000 * 60 * 60 * 24, true);
        }
        return "redirect:http://cart.gmall.com:8084/success.html";
    }

    private BigDecimal getTotalSum(List<CartInfo> cartInfos) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfos) {
            String isChecked = cartInfo.getIsChecked();
            if ("1".equals(isChecked)) {
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }

        return totalPrice;
    }

    private boolean if_new_cart(List<CartInfo> cartInfos, CartInfo cartInfo) {
        boolean b = true;

        for (CartInfo info : cartInfos) {
            String skuId = info.getSkuId();
            if (skuId.equals(cartInfo.getSkuId())) {
                b = false;
            }
        }
        return b;
    }
}
