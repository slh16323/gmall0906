package com.atguigu.gmall.usermanage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserInfoHandler {

    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("user/list")
    public List<UserInfo> getAllUser() {

        List<UserInfo> userInfos = userInfoService.getUserList();

        return userInfos;
    }

}
