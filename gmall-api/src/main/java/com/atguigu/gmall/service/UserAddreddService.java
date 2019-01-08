package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;

import java.util.List;

public interface UserAddreddService {
    List<UserAddress> getUserAddressByUserInfoId(Integer userId);
}
