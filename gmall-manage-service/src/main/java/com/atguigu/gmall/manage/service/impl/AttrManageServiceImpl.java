package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.AttrManageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class AttrManageServiceImpl implements AttrManageService {

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseCatalog1> getCatalog1() {

        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {

        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2s = baseCatalog2Mapper.select(baseCatalog2);

        return baseCatalog2s;


    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {

        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3s = baseCatalog3Mapper.select(baseCatalog3);

        return baseCatalog3s;


    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.select(baseAttrInfo);

        for (BaseAttrInfo attrInfo : baseAttrInfos) {
            //创建一个BaseAttrValue对象
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            //设置attrId
            baseAttrValue.setAttrId(attrInfo.getId());
            //查询属性值列表
            List<BaseAttrValue> attrValues = baseAttrValueMapper.select(baseAttrValue);
            //设置BaseAttrInfo对应的属性值集合
            attrInfo.setAttrValueList(attrValues);

        }

        return baseAttrInfos;
    }

    @Override
    public void savaAttr(BaseAttrInfo baseAttrInfo) {

        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {

            //只持久化对象中有值属性
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //获取属性的id
        String baseAttrInfoId = baseAttrInfo.getId();
        //获取属性值得list集合
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            //循环遍历属性值列表，进行插入
            for (BaseAttrValue baseAttrValue : attrValueList) {

                //防止主键被赋上一个空字符串
                if (baseAttrValue.getId() != null && baseAttrValue.getId().length() == 0) {
                    baseAttrValue.setId(null);
                }

                //设置属性值对应的属性id
                baseAttrValue.setAttrId(baseAttrInfoId);
                //插入属性值
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }

    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

        BaseAttrValue baseAttrValue = new BaseAttrValue();

        baseAttrValue.setAttrId(attrId);

        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.select(baseAttrValue);

        return baseAttrValues;
    }

    @Override
    public void deleteAttr(BaseAttrInfo baseAttrInfo) {


        BaseAttrValue baseAttrValue = new BaseAttrValue();

        String attrId = baseAttrInfo.getId();
        baseAttrValue.setAttrId(attrId);
        baseAttrValueMapper.delete(baseAttrValue);

        baseAttrInfoMapper.delete(baseAttrInfo);

    }
}
