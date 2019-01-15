package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SkuServiceImpl implements SkuInfoService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {

        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setSpuId(spuId);

        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);

        return skuInfos;
    }

    @Override
    public void savaSku(SkuInfo skuInfo) {

    }
}
