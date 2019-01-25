package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.util.ManageUploadUtil;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SpuController {

    @Reference
    private SpuInfoService spuInfoService;

    @RequestMapping("deleteSpuInfo")
    @ResponseBody
    public String deleteSpuInfo(@RequestParam("spuId")String spuId){

        spuInfoService.deleteSpuInfo(spuId);
        return "success";
    }

    @RequestMapping("deleteSpuSaleAttr")
    @ResponseBody
    public List<SpuSaleAttr> deleteSpuSaleAttr(@RequestParam("spuId") String spuId,
                                       @RequestParam("saleAttrId") String saleAttrId){

        List<SpuSaleAttr> spuSaleAttrs = spuInfoService.deleteSpuSaleAttr(spuId,saleAttrId);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }

        return spuSaleAttrs;

    }


    @RequestMapping("deleteSpuImg")
    @ResponseBody
    public List<SpuImage> deleteSpuImg(@RequestParam("spuId") String spuId,
                                       @RequestParam("imgId") String imgId){

        List<SpuImage> spuImages = spuInfoService.deleteSpuImg(spuId,imgId);

        return spuImages;
    }

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

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            // String spuSaleAttrValueJson = JSON.toJSONString(map);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }


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
