package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;

import java.util.List;

public interface SpuInfoService {
    List<SpuInfo> getSpuList(String catalog3Id);

    List<BaseSaleAttr> getBaseSaleAttrList();

    void savaSpu(SpuInfo spuInfo);

    List<SpuSaleAttr> spuSaleAttrList(String spuId);

    List<SpuImage> spuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(String skuId, String spuId);

    List<SpuImage> deleteSpuImg(String spuId, String imgId);

    List<SpuSaleAttr> deleteSpuSaleAttr(String spuId, String saleAttrId);

    void deleteSpuInfo(String spuId);
}
