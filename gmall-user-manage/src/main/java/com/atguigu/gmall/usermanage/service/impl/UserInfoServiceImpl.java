package com.atguigu.gmall.usermanage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> getUserList() {
        return userInfoMapper.selectAll();
    }


    @Override
    public UserInfo getUserInfoById(Integer userId) {
        return userInfoMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void updateUserInfo(UserInfo user) {
        userInfoMapper.updateByPrimaryKeySelective(user);
    }

    @Override
    public void deleteUserInfo(Integer userId) {
        userInfoMapper.deleteByPrimaryKey(userId);
    }
}
