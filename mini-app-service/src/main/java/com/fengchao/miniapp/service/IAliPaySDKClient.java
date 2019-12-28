package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.AliPayRefundRespBean;
import com.fengchao.miniapp.bean.AliPaySignParamBean;
import com.fengchao.miniapp.bean.AliWapPayPostBean;
import com.fengchao.miniapp.bean.WeChatTokenResultBean;
import com.fengchao.miniapp.model.AliPayConfig;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface IAliPaySDKClient {

    String postWapPay(AliWapPayPostBean data, String iAppId);

    AliPaySignParamBean
    signParam(Map<String,String> map,String iAppId);

    boolean
    verifySign(Map<String,String> map);

    WeChatTokenResultBean
    getToken(String code,String iAppId) throws Exception;

    AliPayConfig
    getAppIdConfig(String iAppId);

    AliPayRefundRespBean
    refund(String tradeNo, Float amount,String iAppId) throws Exception;

    Payment
    handlePaymentNotify(Map<String,String> params);

    Refund
    queryRefund(Refund refund);

    Payment
    queryPayment(Payment record);

    Map<String,String>
    request2Map(HttpServletRequest request);
}
