package com.atguigu.gmall.usermanage.service;

import com.atguigu.gmall.usermanage.bean.UserAddress;

import java.util.List;

public interface UserAddreddService {

    List<UserAddress> getUserAddressByUserInfoId(Integer userId);
}
