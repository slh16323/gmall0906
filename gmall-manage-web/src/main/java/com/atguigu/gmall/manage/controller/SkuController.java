package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.SkuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SkuController {

    @Reference
    SkuInfoService skuInfoService;


    @RequestMapping("savaSku")
    @ResponseBody
    public String savaSku(SkuInfo skuInfo){

        //skuInfoService.savaSku(skuInfo);

        return "success";
    }

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(String spuId){

        List<SkuInfo> skuInfos = skuInfoService.skuInfoListBySpu(spuId);

        return skuInfos;
    }
}
