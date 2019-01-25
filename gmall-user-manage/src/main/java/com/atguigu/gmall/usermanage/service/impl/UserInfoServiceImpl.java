package com.atguigu.gmall.usermanage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    UserAddressMapper userAddressMapper;

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

    @Override
    public UserInfo login(UserInfo userInfo) {

        UserInfo userParam = new UserInfo();
        userParam.setLoginName(userInfo.getLoginName());
        userParam.setPasswd(userInfo.getPasswd());

        userParam = userInfoMapper.selectOne(userParam);

        return userParam;
    }

    @Override
    public void addToCache(UserInfo user) {
        Jedis jedis = redisUtil.getJedis();
        String userKey = "user:" + user.getId() + ":info";
        //将用户信息存入缓存
        jedis.setex(userKey, 60 * 60 * 24, JSON.toJSONString(user));
    }

    @Override
    public List<UserAddress> getAddressListByUserId(String userId) {

        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);

        List<UserAddress> userAddressList = userAddressMapper.select(userAddress);

        return userAddressList;
    }

    @Override
    public UserAddress getAddressById(String deliveryAddressId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(deliveryAddressId);

        userAddress = userAddressMapper.selectOne(userAddress);

        return userAddress;
    }
}
