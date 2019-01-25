package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.CartInfoService;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.JwtUtil;
import com.atguigu.gmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserInfoService userInfoService;

    @Reference
    CartInfoService cartInfoService;


    @RequestMapping("varify")
    @ResponseBody
    public String varify(String token, String salt) {

        //验证token令牌
        Map userMap = JwtUtil.decode("gmall0906", token, MD5Util.md5(salt));
        HashMap<String, String> map = new HashMap<>();
        if (userMap != null) {
            String userId = (String) userMap.get("userId");
            map.put("userId", userId);
            map.put("result", "success");
            return JSON.toJSONString(map);
        }else {
            return "fail";
        }
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo, HttpServletRequest request, HttpServletResponse response) {

        UserInfo user = userInfoService.login(userInfo);

        String token = "";

        if (user != null) {
            //获取请求的ip
            //nginx中的IP
            String remoteAddr = request.getHeader("request-forwared-for");
            if (StringUtils.isBlank(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
                if (StringUtils.isBlank(remoteAddr)) {
                    remoteAddr = "127.0.0.1";
                }
            }
            //封装用户信息
            Map<String, String> map = new HashMap<>();
            map.put("userId", user.getId());
            map.put("nickName", user.getNickName());
            //MD5加密ip地址生成盐值
            String salt = MD5Util.md5(remoteAddr);
            //生成token令牌
            token = JwtUtil.encode("gmall0906", map, salt);

            //将用户的信息加入到缓存中
            userInfoService.addToCache(user);
            //合并cookie和数据库中的购物车
            String listCartCookie = CookieUtil.getCookieValue(request, "listCartCookie", true);
            if (StringUtils.isNotBlank(listCartCookie)) {
                //合并购物车
                cartInfoService.merge(user.getId(), listCartCookie);
            } else {
                //刷新缓存
                cartInfoService.flushCart(user.getId());
            }
            //清除cookie中购物车信息
            CookieUtil.deleteCookie(request,response,"listCartCookie");
        } else {
            //验证失败
            return "fail";
        }
        return token;
    }

    @RequestMapping("index")
    public String index(String originUrl, ModelMap map) {

        map.put("originUrl", originUrl);

        return "index";
    }
}
