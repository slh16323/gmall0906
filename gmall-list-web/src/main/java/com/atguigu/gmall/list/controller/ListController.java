package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrManageService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListController {

    @Reference
    ListService listService;

    @Reference
    AttrManageService attrManageService;

    @RequestMapping("list.html")
    public String list(SkuLsParam skuLsParam, ModelMap map){

        SkuLsResult skuLsResults = listService.getSkuLsInfos(skuLsParam);
        List<SkuLsInfo> skuLsInfos = skuLsResults.getSkuLsInfoList();
        //封装平台属性列表,使用set集合进行去重
        Set<String> attrValueIds = new HashSet<>();
        //遍历skuLsInfos,获取每一个skuLsInfo对象
        for (SkuLsInfo skuLsInfo : skuLsInfos) {
            //获取SkuLsAttrValue的集合
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            //遍历集合获取valueId
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                //将valueId添加到set集合中去重
                attrValueIds.add(valueId);
            }
        }

        //使用工具类将attrValueIds的集合用‘，’分割
        String attrValueId = StringUtils.join(attrValueIds, ",");
        //调用远程方法，平台属性和平台属性值
        List<BaseAttrInfo> attrList = new ArrayList<>();
        attrList =attrManageService.getAttrListByValueIds(attrValueId);

        //面包屑功能，selectedValuelist存储选中的平台属性
        List<BaseAttrValueExt> selectedValuelist = new ArrayList<>();
        //去掉已经提交的平台属性值id
        String[] valueId = skuLsParam.getValueId();
        //判断有没有提交的id
        if (valueId != null && valueId.length>0){
            //对平台属性进行迭代
            Iterator<BaseAttrInfo> iterator = attrList.iterator();
            //判断是否还有下一个元素
            while (iterator.hasNext()) {
                //获取迭代到的元素
                BaseAttrInfo attrInfo = iterator.next();
                //获取属性值的集合
                List<BaseAttrValue> attrValueList = attrInfo.getAttrValueList();
                //遍历属性值的集合
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    //将该平台属性值的id和已经提交了的id进行比较
                    String id = baseAttrValue.getId();
                    for (String sid : valueId) {
                        if (id.equals(sid)) {
                            //创建面包屑对象
                            BaseAttrValueExt baseAttrValueExt = new BaseAttrValueExt();
                            //设置面包屑的属性id
                            baseAttrValueExt.setAttrId(attrInfo.getId());
                            //设置属性名称
                            baseAttrValueExt.setAttrName(attrInfo.getAttrName());
                            //设置属性值id
                            baseAttrValueExt.setId(baseAttrValue.getId());
                            //设置属性值名称
                            baseAttrValueExt.setValueName(baseAttrValue.getValueName());
                            //获取面包屑的URL地址
                            String myUrlParam = getMyUrlParam(skuLsParam,sid);
                            //设置面包屑的跳转地址
                            baseAttrValueExt.setCancelUrlParam(myUrlParam);
                            //添加到面包屑的集合
                            selectedValuelist.add(baseAttrValueExt);
                            //如果匹配则去掉id所在的属性
                            iterator.remove();
                        }
                    }
                }

            }
        }

        //根据当前的请求参数对象生成请求参数字符串
        String urlParam = getMyUrlParam(skuLsParam,"");

        //分页功能

        Integer totalPages = skuLsResults.getTotal();
        map.put("totalPages",totalPages);
        map.put("pageNo",skuLsParam.getPageNo());

        //将结果存入模型中
        map.put("attrList", attrList);
        map.put("urlParam", urlParam);
        map.put("attrValueSelectedList", selectedValuelist);
        map.put("skuLsInfoList",skuLsInfos);

        return "list";
    }

    private String getMyUrlParam(SkuLsParam skuLsParam,String sid) {

        String urlParam = "";
        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueId = skuLsParam.getValueId();

        if (StringUtils.isNoneBlank(catalog3Id)) {
            urlParam = urlParam + "&catalog3Id=" + catalog3Id;
        }
        if (StringUtils.isNoneBlank(keyword)) {
            urlParam = urlParam + "&keyword=" + keyword;
        }
        if (valueId != null && valueId.length > 0) {
            for (String id : valueId) {
                if (!sid.equals(id)){
                    urlParam = urlParam + "&valueId=" + id;
                }
            }
        }
        if (urlParam != null && urlParam.length()>0){

            urlParam = urlParam.substring(1);
        }

        return urlParam;
    }

}
