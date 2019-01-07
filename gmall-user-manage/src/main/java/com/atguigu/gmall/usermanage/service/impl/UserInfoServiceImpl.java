package com.atguigu.gmall.usermanage.service.impl;


import com.atguigu.gmall.usermanage.bean.UserInfo;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import com.atguigu.gmall.usermanage.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
