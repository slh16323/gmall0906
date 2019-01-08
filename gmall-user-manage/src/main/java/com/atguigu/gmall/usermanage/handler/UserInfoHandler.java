package com.atguigu.gmall.usermanage.handler;


import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserInfoHandler {
    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("user/list")
    public List<UserInfo> getAllUser() {

        List<UserInfo> userInfos = userInfoService.getUserList();

        return userInfos;
    }

}
