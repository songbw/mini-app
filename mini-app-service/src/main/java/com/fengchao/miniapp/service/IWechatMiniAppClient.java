package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;

import java.util.Map;

public interface IWechatMiniAppClient {
    WeChatTokenResultBean getAccessToken(String apiType, String iAppId)throws Exception;

    WeChatSessionResultBean getSession(String jsCode,String apiType,String iAppId)throws Exception;

    String signParam(Map<String,Object> params) throws Exception;

    WechatPrepayBean postPrepayId(WechatOrderPostBean data, String ip, String apiType,String iAppId) throws Exception;

    Map<String,Object> verifySign(String xmlStr) throws Exception;

    Refund
    postRefund(Refund refund,WechatRefundPostBean data,String apiType, String iAppId) throws Exception;

    void
    queryRefund(WechatRefundListBean refund,String apiType, String iAppId) throws Exception;

    void
    queryRefundStatus(Refund refund, String apiType,String iAppId) throws Exception;

    void
    queryPayment(Payment payment, String apiType) throws Exception;

    boolean
    closePayment(Payment payment, String apiType) throws Exception;

    String
    getAppId(String apiType,String iAppId) throws Exception;

    String
    getPayType(String apiType);

    boolean
    isValidIAppId(String iAppId);

    void
    updateWeChatConfigByVendor(String renterId, String wechatId);
}
