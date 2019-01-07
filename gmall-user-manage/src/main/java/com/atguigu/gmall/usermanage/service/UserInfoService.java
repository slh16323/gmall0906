package com.atguigu.gmall.usermanage.service;

import com.atguigu.gmall.usermanage.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    List<UserInfo> getUserList();

    UserInfo getUserInfoById(Integer userId);

    void updateUserInfo(UserInfo user);

    void deleteUserInfo(Integer userId);
}
