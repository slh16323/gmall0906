package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuInfoServiceImpl implements SpuInfoService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        List<SpuInfo> spuInfos = spuInfoMapper.select(spuInfo);

        return spuInfos;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        List<BaseSaleAttr> baseSaleAttrs = baseSaleAttrMapper.selectAll();

        return baseSaleAttrs;
    }

    @Override
    public void savaSpu(SpuInfo spuInfo) {
        //插入spuInfo
        spuInfoMapper.insertSelective(spuInfo);
        //获取数据插入后的返回的主键id
        String spuInfoId = spuInfo.getId();

        //获取图片信息
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //循环遍历插入图片信息
        for (SpuImage spuImage : spuImageList) {
            //设置spuId
            spuImage.setSpuId(spuInfoId);
            //插入数据
            spuImageMapper.insertSelective(spuImage);
        }

        //获取属性信息
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //循环遍历插入属性信息
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            //设置spuId
            spuSaleAttr.setSpuId(spuInfoId);
            //插入数据
            spuSaleAttrMapper.insertSelective(spuSaleAttr);
            //获取属性值的数据
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            //循环遍历插入属性值
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                //设置spuId
                spuSaleAttrValue.setSpuId(spuInfoId);
                //插入属性值得数据
                spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
            }

        }


    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {

        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.select(spuSaleAttr);

        for (SpuSaleAttr saleAttr : spuSaleAttrs) {
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();

            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());

            List<SpuSaleAttrValue> saleAttrValues = spuSaleAttrValueMapper.select(spuSaleAttrValue);

            saleAttr.setSpuSaleAttrValueList(saleAttrValues);

        }
        return spuSaleAttrs;


    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {

        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);

        List<SpuImage> spuImages = spuImageMapper.select(spuImage);

        return spuImages;
    }
}
