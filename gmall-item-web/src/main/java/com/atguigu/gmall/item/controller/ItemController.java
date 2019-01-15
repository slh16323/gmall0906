package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SpuInfoService spuInfoService;

    @Reference
    SkuInfoService skuInfoService;

    @RequestMapping("{skuId}.html")
    public String itemList(@PathVariable("skuId") String skuId, ModelMap map) {

        SkuInfo skuInfo = skuInfoService.item(skuId);
        String spuId = skuInfo.getSpuId();

        //获取销售属性的集合
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = new ArrayList<>();
        spuSaleAttrListCheckBySku = spuInfoService.getSpuSaleAttrListCheckBySku(skuId,spuId);

        map.put("spuSaleAttrListCheckBySku", spuSaleAttrListCheckBySku);
        map.put("skuInfo", skuInfo);

        return "item";
    }

    @RequestMapping("index")
    public String index(Model model) {

        model.addAttribute("key", "hello thyemtlfe");

        return "index";
    }
}
