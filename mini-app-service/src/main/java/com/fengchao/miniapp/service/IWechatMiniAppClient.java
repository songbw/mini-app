package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;

import java.util.Map;

public interface IWechatMiniAppClient {
    WeChatTokenResultBean getAccessToken()throws Exception;

    WeChatSessionResultBean getSession(String jsCode)throws Exception;

    String signParam(Map<String,Object> params) throws Exception;

    WechatPrepayBean postPrepayId(WechatOrderPostBean data, String ip) throws Exception;

    Map<String,Object> verifySign(String xmlStr) throws Exception;

    Refund
    postRefund(WechatRefundPostBean data) throws Exception;

    void
    queryRefund(WechatRefundListBean refund) throws Exception;

    void
    queryRefundStatus(Refund refund) throws Exception;

    void
    queryPayment(Payment payment) throws Exception;

    boolean
    closePayment(Payment payment) throws Exception;
}
