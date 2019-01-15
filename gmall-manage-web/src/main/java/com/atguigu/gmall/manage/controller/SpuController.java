package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.manage.util.ManageUploadUtil;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {

    @Reference
    private SpuInfoService spuInfoService;


    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){

        List<SpuImage> spuImages = spuInfoService.spuImageList(spuId);

        return spuImages;

    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        List<SpuSaleAttr> spuSaleAttrs = spuInfoService.spuSaleAttrList(spuId);

        return spuSaleAttrs;

    }

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        //调用上传工具类进行上传
        String imgurl = ManageUploadUtil.imgUploda(multipartFile);

        return imgurl;
    }

    @RequestMapping("savaSpu")
    @ResponseBody
    public String savaSpu(SpuInfo spuInfo){

        spuInfoService.savaSpu(spuInfo);

        return "success";

    }

    @RequestMapping("spuList")
    @ResponseBody
    public List<SpuInfo> spuList(String catalog3Id){

        List<SpuInfo> spuInfos =spuInfoService.getSpuList(catalog3Id);

        return spuInfos;

    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){

        List<BaseSaleAttr> baseSaleAttrs =spuInfoService.getBaseSaleAttrList();

        return baseSaleAttrs;

    }



}
