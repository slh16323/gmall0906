package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.SkuInfoService;
import com.atguigu.gmall.service.SpuInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
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


        List<SkuInfo> skuInfos = skuInfoService.getSkuSaleAttrValueListBySpu(spuId);

        HashMap<String, String> skuMap = new HashMap<>();

        for (SkuInfo info : skuInfos) {
            String v = info.getId();
            List<SkuSaleAttrValue> skuAttrValueList = info.getSkuSaleAttrValueList();

            String k = "";

            for (SkuSaleAttrValue skuSaleAttrValue : skuAttrValueList) {
                k = k+skuSaleAttrValue.getSaleAttrValueId()+'|';
            }

            skuMap.put(k, v);

        }


        map.put("skuMap", JSON.toJSONString(skuMap));

        map.put("spuSaleAttrListCheckBySku", spuSaleAttrListCheckBySku);
        map.put("skuInfo", skuInfo);

        return "item";
    }

    @RequestMapping("index")
    public String index(Model model) {

        model.addAttribute("key", "hello thyemtlfe");

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add("你好" + i);
        }

        List<SkuInfo> skuInfos = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            SkuInfo skuInfo = new SkuInfo();
            skuInfo.setSkuName("sku"+i);
            skuInfos.add(skuInfo);
        }

        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setSpuName("nihaohaiyu");
        model.addAttribute("spuInfo", null);

        model.addAttribute("skuInfo", skuInfos);

        model.addAttribute("list", list);
        return "index";
    }
}
