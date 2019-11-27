package com.fengchao.miniapp.model;

import java.util.Date;

public class Refund {
    private Long id;

    private String openId;

    private String refundNo;

    private String wechatRefundNo;

    private String transactionId;

    private String orderId;

    private Integer totalFee;

    private Integer refundFee;

    private Integer respRefundFee;

    private Integer refundCashFee;

    private Integer cashFee;

    private String status;

    private String comments;

    private String refundAccount;

    private String refundRecvAccount;

    private String successTime;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId == null ? null : openId.trim();
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo == null ? null : refundNo.trim();
    }

    public String getWechatRefundNo() {
        return wechatRefundNo;
    }

    public void setWechatRefundNo(String wechatRefundNo) {
        this.wechatRefundNo = wechatRefundNo == null ? null : wechatRefundNo.trim();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId == null ? null : transactionId.trim();
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId == null ? null : orderId.trim();
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(Integer refundFee) {
        this.refundFee = refundFee;
    }

    public Integer getRespRefundFee() {
        return respRefundFee;
    }

    public void setRespRefundFee(Integer respRefundFee) {
        this.respRefundFee = respRefundFee;
    }

    public Integer getRefundCashFee() {
        return refundCashFee;
    }

    public void setRefundCashFee(Integer refundCashFee) {
        this.refundCashFee = refundCashFee;
    }

    public Integer getCashFee() {
        return cashFee;
    }

    public void setCashFee(Integer cashFee) {
        this.cashFee = cashFee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments == null ? null : comments.trim();
    }

    public String getRefundAccount() {
        return refundAccount;
    }

    public void setRefundAccount(String refundAccount) {
        this.refundAccount = refundAccount == null ? null : refundAccount.trim();
    }

    public String getRefundRecvAccount() {
        return refundRecvAccount;
    }

    public void setRefundRecvAccount(String refundRecvAccount) {
        this.refundRecvAccount = refundRecvAccount == null ? null : refundRecvAccount.trim();
    }

    public String getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(String successTime) {
        this.successTime = successTime == null ? null : successTime.trim();
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
}