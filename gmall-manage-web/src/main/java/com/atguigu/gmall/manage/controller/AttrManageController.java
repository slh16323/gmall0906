package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrManageController {

    @Reference
    AttrManageService attrManageService;


    @RequestMapping("deleteAttr")
    @ResponseBody
    public String deleteAttr(BaseAttrInfo baseAttrInfo){

        attrManageService.deleteAttr(baseAttrInfo);

        return "删除成功";
    }

    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        List<BaseAttrValue> baseAttrValues = attrManageService.getAttrValueList(attrId);

        return baseAttrValues;
    }

    @RequestMapping("saveAttr")
    @ResponseBody
    public String saveAttr(BaseAttrInfo baseAttrInfo){

        attrManageService.savaAttr(baseAttrInfo);

        return "SUCCESS";
    }

    @RequestMapping("getAttrList")
    @ResponseBody
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        List<BaseAttrInfo> baseAttrInfos = attrManageService.getAttrList(catalog3Id);

        return baseAttrInfos;
    }

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1() {

        List<BaseCatalog1> baseCatalog1s = attrManageService.getCatalog1();


        return baseCatalog1s;
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        List<BaseCatalog2> baseCatalog2s = attrManageService.getCatalog2(catalog1Id);

        return baseCatalog2s;
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {

        List<BaseCatalog3> baseCatalog3s = attrManageService.getCatalog3(catalog2Id);

        return baseCatalog3s;
    }

}
