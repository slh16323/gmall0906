package com.atguigu.gmall.usermanage.service.impl;

import com.atguigu.gmall.usermanage.bean.UserAddress;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.service.UserAddreddService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class UserAddressServiceImpl implements UserAddreddService {

    @Autowired
    private UserAddressMapper userAddressMapper;


    @Override
    public List<UserAddress> getUserAddressByUserInfoId(Integer userId) {
        return userAddressMapper.selectByExample(userId);
    }
}
