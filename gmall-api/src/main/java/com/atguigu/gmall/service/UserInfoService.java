package com.atguigu.gmall.service;


import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserInfoService {

    List<UserInfo> getUserList();

    UserInfo getUserInfoById(Integer userId);

    void updateUserInfo(UserInfo user);

    void deleteUserInfo(Integer userId);

    UserInfo login(UserInfo userInfo);

    void addToCache(UserInfo user);

    List<UserAddress> getAddressListByUserId(String userId);

    UserAddress getAddressById(String deliveryAddressId);
}
