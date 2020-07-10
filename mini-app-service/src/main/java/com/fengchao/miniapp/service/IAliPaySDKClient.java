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
    signParam(Map<String,String> map, AliPayConfig config);

    boolean
    verifySign(Map<String,String> map,String iAppId);

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

    /**
     * 支付宝app支付接口2.0(alipay.trade.app.pay),拼接参数，签名，并编码一级key的value(包括sign)
     *
     * @param iAppId 端id
     * @param outTradeNo 订单号
     * @param subject 交易名
     * @param totalAmount 支付金额（分），会转换为元后发给支付宝
     * @param returnUrl app通知url
     * @return string app_id=2021001146662264&biz_content=%7B%22out_trade_no%22%3A%2211201900011236%22%2C%22product_code%22%3A%22QUICK_MSECURITY_PAY%22%2C%22subject%22%3A%22%E5%A4%A7%E4%B9%90%E9%80%8F%22%2C%22timeout_express%22%3A%22120m%22%2C%22total_amount%22%3A%220.01%22%7D&charset=utf-8&method=alipay.trade.app.pay&notify_url=https%3A%2F%2Fapi.weesharing.com%2Fv2%2Fminiapp%2FaliPay%2Fpayment%2Fnotify&sign=l5dGnfNLfmrKb0xr1ZNglIkisqL3F2A9Sxgfg%2FI%2FsyqgRZ2MFTH%2Fb2BFYrZcqUGQ%2BJXvdfdkS6Nafi073Q0Oz3VkXThyZtV6CPzYZ168vbPgdo0NH4FUSV2qsaGSqCXFJ7Md4Y4VpRe4S37iU21Qy7Iw2a2zqalPOHfSDUnXN%2BON8j5A9TBej9WG8K%2BJ%2BHLbZbjdwuOg1U7nWHr%2F33LGFrnfuhn4YleQ7dxUWgqp8CmhN%2BIKmlU18uSMndzcZZUVZExFpKvvkSxBQXRK6S6TsNp6El6VwA6Ymenh5ToFwtzgT4KahyplBxd6l8lhdgrPkb86lxDasGo4WllOK4%2Fksw%3D%3D&sign_type=RSA2&timestamp=2020-07-09+23%3A55%3A11&version=1.0
     * */
    String
    buildJSSDKPayString(String iAppId,String outTradeNo,String totalAmount,String subject,String returnUrl);
}
