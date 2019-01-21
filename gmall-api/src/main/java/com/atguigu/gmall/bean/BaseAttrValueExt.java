package com.atguigu.gmall.bean;

public class BaseAttrValueExt extends BaseAttrValue {
    private String attrName;
    private String cancelUrlParam;

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getCancelUrlParam() {
        return cancelUrlParam;
    }

    public void setCancelUrlParam(String cancelUrlParam) {
        this.cancelUrlParam = cancelUrlParam;
    }
}
