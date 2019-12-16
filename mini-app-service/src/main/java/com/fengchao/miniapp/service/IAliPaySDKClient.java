package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.AliPayRefundRespBean;
import com.fengchao.miniapp.bean.AliPaySignParamBean;
import com.fengchao.miniapp.bean.WeChatTokenResultBean;
import com.fengchao.miniapp.model.Payment;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IAliPaySDKClient {

    AliPaySignParamBean
    signParam(Map<String,String> map,String iAppId);

    boolean
    verifySign(Map<String,String> map);

    WeChatTokenResultBean
    getToken(String code,String iAppId) throws Exception;

    String
    getAppId(String iAppId);

    AliPayRefundRespBean
    refund(String tradeNo, Float amount,String iAppId) throws Exception;

    Payment
    handlePaymentNotify(HttpServletRequest request);
}
