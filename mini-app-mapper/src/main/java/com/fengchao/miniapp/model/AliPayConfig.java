package com.fengchao.miniapp.model;

import java.util.Date;

public class AliPayConfig {
    private Integer id;

    private String iAppId;

    private String payAppId;

    private String payNotify;

    private String refundNotify;

    private String apiUrl;

    private Date createTime;

    private Date updateTime;

    private String comments;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getiAppId() {
        return iAppId;
    }

    public void setiAppId(String iAppId) {
        this.iAppId = iAppId == null ? null : iAppId.trim();
    }

    public String getPayAppId() {
        return payAppId;
    }

    public void setPayAppId(String payAppId) {
        this.payAppId = payAppId == null ? null : payAppId.trim();
    }

    public String getPayNotify() {
        return payNotify;
    }

    public void setPayNotify(String payNotify) {
        this.payNotify = payNotify == null ? null : payNotify.trim();
    }

    public String getRefundNotify() {
        return refundNotify;
    }

    public void setRefundNotify(String refundNotify) {
        this.refundNotify = refundNotify == null ? null : refundNotify.trim();
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl == null ? null : apiUrl.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments == null ? null : comments.trim();
    }
}