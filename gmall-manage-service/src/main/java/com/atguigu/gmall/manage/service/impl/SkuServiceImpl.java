package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.constants.RedisConst;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkuServiceImpl implements SkuInfoService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public List<SkuInfo> skuInfoListBySpu(String spuId) {

        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setSpuId(spuId);

        List<SkuInfo> skuInfos = skuInfoMapper.select(skuInfo);

        return skuInfos;
    }

    @Override
    public void savaSku(SkuInfo skuInfo) {

        //插入sku基本信息
        skuInfoMapper.insertSelective(skuInfo);
        //获取skuId
        String skuId = skuInfo.getId();

        //插入图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();

        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insertSelective(skuImage);
        }

        //插入关联平台属性信息
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();

        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }

        //插入关联销售属性信息
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();

        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }


    @Override
    public SkuInfo itemFromDB(String skuId) {
        //查询对应skuId的skuInfo
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        SkuInfo info = skuInfoMapper.selectOne(skuInfo);

        //skuInfo对应的图片集合
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImages = skuImageMapper.select(skuImage);

        //将查询到的图片集合封装到skuInfo中
        info.setSkuImageList(skuImages);

        return info;

    }


    @Override
    public SkuInfo item(String skuId) {

        SkuInfo skuInfo = null;
        //首先尝试从缓存中取出数据
        Jedis jedis = redisUtil.getJedis();
        //拼接Redis的key键
        String skuKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKU_SUFFIX;
        //根据key值从Redis中尝试获取对应的数据
        String skuInfoString = jedis.get(skuKey);
        //将取出的字符串对象转为json对象
        skuInfo = JSON.parseObject(skuInfoString, SkuInfo.class);

        //进行判断，缓存中是否有数据
        if (skuInfo == null) {
            //获取分布式锁
            String skuLockKey = RedisConst.SKU_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
            String lock = jedis.set(skuLockKey, "OK", "nx", "px", RedisConst.SKUKEY_TIMEOUT);

            if ("OK".equals(lock)) {
                //拿到了分布式锁
                //缓存中没有数据，直接从数据库中查找
                skuInfo = itemFromDB(skuId);
                if (skuInfo == null) {
                    return null;
                }
            } else {
                //没有拿到分布式锁，等一会，继续申请
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //开始自旋，递归
                jedis.close();
                return item(skuId);
            }
            //归还分布式锁
            jedis.del(skuLockKey);
            //将结果设置进缓存中
            jedis.set(skuKey, JSON.toJSONString(skuInfo));

        }

        jedis.close();

        return skuInfo;

    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {

        List<SkuInfo> skuInfoList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        String key = RedisConst.SKU_LIST_PREFIX + spuId + RedisConst.SKU_SUFFIX;
        Integer llen = new Integer(jedis.llen(key).toString());

        if (llen==0){
            skuInfoList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);

            for (SkuInfo skuInfo : skuInfoList) {
                jedis.lpush(key, JSON.toJSONString(skuInfo));
            }
        }else {
            for (int i = llen; i >0; i--) {
                String skusaleAttr = jedis.lindex(key, i - 1);
                SkuInfo skuInfo = JSON.parseObject(skusaleAttr, SkuInfo.class);
                skuInfoList.add(skuInfo);
            }
        }

        return skuInfoList;
    }

    @Override
    public List<SkuInfo> skuList() {

        List<SkuInfo> skuInfos = skuInfoMapper.selectAll();

        for (SkuInfo skuInfo : skuInfos) {
            SkuAttrValue skuAttrValue = new SkuAttrValue();
            skuAttrValue.setSkuId(skuInfo.getId());
            //获取平台属性id
            List<SkuAttrValue> attrValues = skuAttrValueMapper.select(skuAttrValue);
            //将平台属性存放到skuInfo中
            skuInfo.setSkuAttrValueList(attrValues);
        }

        return skuInfos;
    }
}
