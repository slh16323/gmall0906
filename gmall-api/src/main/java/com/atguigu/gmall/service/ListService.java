package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.bean.SkuLsResult;

public interface ListService {
    SkuLsResult getSkuLsInfos(SkuLsParam skuLsParam);
}
