package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;

import java.util.Map;

public interface IWechatMiniAppClient {
    WeChatTokenResultBean getAccessToken(String apiType)throws Exception;

    WeChatSessionResultBean getSession(String jsCode,String apiType)throws Exception;

    String signParam(Map<String,Object> params) throws Exception;

    WechatPrepayBean postPrepayId(WechatOrderPostBean data, String ip, String apiType) throws Exception;

    Map<String,Object> verifySign(String xmlStr) throws Exception;

    Refund
    postRefund(WechatRefundPostBean data,String apiType) throws Exception;

    void
    queryRefund(WechatRefundListBean refund,String apiType) throws Exception;

    void
    queryRefundStatus(Refund refund, String apiType) throws Exception;

    void
    queryPayment(Payment payment, String apiType) throws Exception;

    boolean
    closePayment(Payment payment, String apiType) throws Exception;

    String
    getAppId(String apiType);
}
