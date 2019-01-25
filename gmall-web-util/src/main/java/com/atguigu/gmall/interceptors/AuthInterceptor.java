package com.atguigu.gmall.interceptors;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.annotations.LoginRequried;
import com.atguigu.gmall.util.CookieUtil;
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //验证是否需要登录
        HandlerMethod hm = (HandlerMethod) handler;
        LoginRequried methodAnnotation = hm.getMethodAnnotation(LoginRequried.class);
        String token = "";
        //获取token
        String newToken = request.getParameter("newToken");
        //从cookie中获取token
        String oldToken = CookieUtil.getCookieValue(request, "oldToken", true);

        if (StringUtils.isNotBlank(oldToken)) {
            token = oldToken;
        }

        if (StringUtils.isNotBlank(newToken)) {
            token = newToken;
        }

        //通过注解,判断当前服务是否需要登录
        if (methodAnnotation != null) {
            //获取是否需要登录
            boolean isLogin = methodAnnotation.autoRedirect();
            //校验token
            boolean result = false;
            //获取浏览器数据，设置盐值
            String remoteAddr = request.getHeader("request-forwared-for");
            if (StringUtils.isBlank(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
                if (StringUtils.isBlank(remoteAddr)) {
                    remoteAddr = "127.0.0.1";
                }
            }

            if (StringUtils.isNotBlank(token)) {
                String success = HttpClientUtil.doGet("http://passport.gmall.com:8085/varify?token=" + token + "&salt=" + remoteAddr);
                if ("fail".equals(success)) {
                    result = false;
                } else {
                    JSONObject jsonObject = JSON.parseObject(success);
                    String resultStatus = (String) jsonObject.get("result");
                    String userId = (String) jsonObject.get("userId");
                    if ("success".equals(resultStatus)) {
                        //验证成功
                        //将token添加到cookie中
                        CookieUtil.setCookie(request, response, "oldToken", token, 60 * 60 * 24, true);
                        //将userId存入request域中
                        request.setAttribute("userId", userId);
                        result = true;
                    }
                }
            }

            if (isLogin == true && result == false) {
                response.sendRedirect("http://passport.gmall.com:8085/index?originUrl=" + request.getRequestURL());
                return false;
            }
        }

        return true;
    }
}
