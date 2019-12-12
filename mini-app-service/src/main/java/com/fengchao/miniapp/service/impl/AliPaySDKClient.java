package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayOpenAuthTokenAppRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayOpenAuthTokenAppResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.config.MiniAppConfiguration;
import com.fengchao.miniapp.constant.*;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.service.IAliPaySDKClient;


import java.util.*;

import com.fengchao.miniapp.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class AliPaySDKClient implements IAliPaySDKClient {

    private MiniAppConfiguration configuration;
    private PaymentServiceImpl paymentService;

    @Autowired
    public AliPaySDKClient(PaymentServiceImpl paymentService,
                           MiniAppConfiguration configuration){

        this.configuration = configuration;
        this.paymentService = paymentService;
    }

    @Override
    public AliPaySignParamBean
    signParam(Map<String,String> map){
        String _func = "支付宝SDK签名: ";
        log.info("{} 入参 {}",_func, JSON.toJSON(map));
        String content = AlipaySignature.getSignCheckContentV2(map);
        log.info("{} 拼接待签名字符串 {}",_func,content);
        String sign;
        try{
            sign = AlipaySignature.rsa256Sign(content, AliPay.FC_PRIVATE_KEY_VALUE,AliPay.CHARSET);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage(),e);
            return null;
        }

        AliPaySignParamBean bean = new AliPaySignParamBean();
        bean.setSign(sign);
        bean.setSignType(AliPay.SIGN_TYPE_VALUE);
        bean.setAppId(getAppId());

        return bean;
    }

    @Override
    public boolean
    verifySign(Map<String,String> map){
        String _func = "支付宝SDK验签: ";
        log.info("{} 入参 {}",_func, JSON.toJSON(map));

        boolean result;

        try {
            result = AlipaySignature.rsaCertCheckV2(map, AliPay.PUBLIC_KEY_VALUE, AliPay.CHARSET, AliPay.SIGN_TYPE_VALUE);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage(),e);
            return false;
        }

        log.info("{} 结果为 ",_func,result);
        return result;
    }

    @Override
    public WeChatTokenResultBean
    getToken(String code) throws Exception {
        String _func = "支付宝获取token: ";
        log.info("{} 入参 {}",_func,code);

        String appId = getAppId();
        Map<String,String> map = new HashMap<>();
        map.put(AliPay.GRANT_TYPE_KEY,AliPay.GRANT_TYPE_AUTH_CODE);
        map.put(AliPay.CODE_KEY,code);

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        appId,AliPay.FC_PRIVATE_KEY_VALUE,
                "json", AliPay.CHARSET,
                        AliPay.PUBLIC_KEY_VALUE,
                        AliPay.SIGN_TYPE_VALUE);
        AlipayOpenAuthTokenAppRequest request = new AlipayOpenAuthTokenAppRequest();
        request.setBizContent(JSON.toJSONString(map));
        AlipayOpenAuthTokenAppResponse resp;

        try {
            resp = alipayClient.execute(request);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage(),e);
            throw e;
        }

        if (null == resp){
            log.error("{} 获取 token 失败",_func);
            throw new Exception(MyErrorCode.ALIPAY_SDK_RESPONSE_NULL);
        }
        if (!resp.isSuccess()){
            log.error("{} 获取 token 失败 {}",_func,resp.getMsg());
            throw new Exception(MyErrorCode.ALIPAY_SDK_GET_TOKEN_FAILED+resp.getMsg());
        }

        WeChatTokenResultBean bean = new WeChatTokenResultBean();
        bean.setAccess_token(resp.getAppAuthToken());
        bean.setExpires_in(Integer.valueOf(resp.getExpiresIn()));
        log.info("{} 获取token={} expires={}",_func,resp.getAppAuthToken(), resp.getExpiresIn());
        return bean;
    }

    @Override
    public String getAppId(){
        return (null == configuration.getAliPayAppId())?
                AliPay.FC_APPID_DEFAULT:configuration.getAliPayAppId();
    }

    @Override
    public AliPayRefundRespBean
    refund(String tradeNo,Float amount) throws Exception{
        String _func = "支付宝退款: ";
        log.info("{} 入参 {}",_func,tradeNo);

        String appId = getAppId();
        String refundNo = RandomString.buildRefundNo(appId);
        Map<String,Object> map = new HashMap<>();
        map.put(AliPay.OUT_TRADE_NO_KEY,tradeNo);
        map.put(AliPay.REFUND_AMOUNT_KEY,amount);
        map.put(AliPay.OUT_REQUEST_NO_KEY, refundNo);

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        appId,AliPay.FC_PRIVATE_KEY_VALUE,
                        "json", AliPay.CHARSET,
                        AliPay.PUBLIC_KEY_VALUE,
                        AliPay.SIGN_TYPE_VALUE);

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse resp;

        try{
            resp = alipayClient.execute(request);
        }catch (AlipayApiException e){
            log.error("{} {}",_func,e.getMessage(),e);
            throw new Exception(MyErrorCode.ALIPAY_SDK_FAILED+e.getErrMsg());
        }

        if (null == resp){
            log.error("{} 失败",_func);
            throw new Exception(MyErrorCode.ALIPAY_SDK_RESPONSE_NULL);
        }
        if (!resp.isSuccess()){
            log.error("{} 失败 {}",_func,resp.getMsg());
            throw new Exception(MyErrorCode.ALIPAY_SDK_REFUND_FAILED+resp.getMsg());
        }

        log.info("{} SDK 返回 {}",_func,JSON.toJSONString(resp));

        AliPayRefundRespBean bean = new AliPayRefundRespBean();
        bean.setRefundNo(refundNo);
        bean.setTradeNo(resp.getOutTradeNo());
        bean.setAliPayTradeNo(resp.getTradeNo());
        bean.setRefundFee(resp.getRefundFee());
        bean.setRefundDate(resp.getGmtRefundPay());
        bean.setAliPayLoginId(resp.getBuyerLogonId());

        log.info("{} 完成 ",_func);
        return bean;
    }

    @Override
    public Payment handlePaymentNotify(HttpServletRequest request){
        String _func = "handlePaymentNotify: ";
        Map<String,String> params = request2Map(request);
        if (!verifySign(params)){
            return null;
        }

        String appId = params.get(AliPay.APP_ID_KEY);
        if (!appId.equals(getAppId())){
            log.error("appId={} 不匹配",appId);
            return null;
        }

        String tradeNo = params.get(AliPay.OUT_TRADE_NO_KEY);
        String aliPayTradeNo = params.get(AliPay.TRADE_NO_KEY);

        String tradeStatue = params.get(AliPay.TRADE_STATUS_KEY);

        PageInfo<Payment> pages = null ;
        Payment record ;
        try{
            pages = paymentService.queryList(1,1,"id","DESC",
                    null,tradeNo,null,null);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage());
        }

        if (null == pages || null == pages.getRows() || 0 == pages.getRows().size()){
            log.error("{} {}",_func,MyErrorCode.PAYMENT_NO_FOUND);
            record = new Payment();
        }else{
            record = pages.getRows().get(0);
            if (PaymentStatusType.OK.getCode().equals(record.getStatus())
                    || PaymentStatusType.FAILED.getCode().equals(record.getStatus())) {

                log.info("{} 支付结果已经记录,不再重复处理", _func);
                return null;
            }
        }

        record.setApiType(ApiType.ALIPAY_PHONE_WEB.getCode());
        record.setCreateTime(new Date());
        record.setUpdateTime(record.getCreateTime());
        record.setOrderId(tradeNo);
        record.setTransactionId(aliPayTradeNo);

        if (null != tradeStatue){
            if( AliPay.TRADE_STATUS_OK.equals(tradeStatue)){
                record.setStatus(PaymentStatusType.OK.getCode());
            }else if (AliPay.TRADE_STATUS_WAIT_PAY.equals(tradeStatue)){
                record.setStatus(PaymentStatusType.PREPAY.getCode());
            }else{
                record.setStatus(PaymentStatusType.FAILED.getCode());
            }
        }

        String receiptFee = params.get(AliPay.RECEIPT_AMOUNT_KEY);
        if (null != receiptFee){
            record.setTotalFee(Integer.valueOf(FeeUtils.Yuan2Fen(receiptFee)));
        }
        String payFee = params.get(AliPay.PAY_AMOUNT_KEY);
        if (null != payFee){
            record.setRespTotalFee(Integer.valueOf(FeeUtils.Yuan2Fen(payFee)));
        }
        String endTime = params.get(AliPay.END_TIME_KEY);
        if (null != endTime){
            record.setTimeEnd(endTime);
        }
        String loginId = params.get(AliPay.BUYER_LOGON_ID_KEY);
        if (null != loginId){
            record.setOpenId(loginId);
        }

        try{
            paymentService.insert(record);
        }catch (Exception e){
            log.error("插入支付记录失败 {}",e.getMessage());
        }

        return record;
    }

    public AliPayRefundQueryResultBean
    queryRefund(Refund refund) throws Exception{
        String _func = "支付宝退款查询 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} orderId= {}",_func,refund.getOrderId());

        String appId = getAppId();
        Map<String,String> map = new HashMap<>();
        map.put(AliPay.OUT_TRADE_NO_KEY,refund.getOrderId());
        map.put(AliPay.OUT_REQUEST_NO_KEY,refund.getRefundNo());

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        appId,AliPay.FC_PRIVATE_KEY_VALUE,
                        "json", AliPay.CHARSET,
                        AliPay.PUBLIC_KEY_VALUE,
                        AliPay.SIGN_TYPE_VALUE);

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeFastpayRefundQueryResponse resp;
        try {
            resp = alipayClient.execute(request);
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage(),e);
            throw e;
        }

        AliPayRefundQueryResultBean bean = new AliPayRefundQueryResultBean();
        String refundFee = resp.getRefundAmount();
        String totalFee = resp.getTotalAmount();
        bean.setOrderId(refund.getOrderId());
        bean.setRefundNo(refund.getRefundNo());
        bean.setTotalFee(FeeUtils.Yuan2Fen(totalFee));
        bean.setRefundFee(FeeUtils.Yuan2Fen(refundFee));

        return bean;
    }


    private Map<String,String>
    request2Map(HttpServletRequest request){

        Map<String, String> retMap = new HashMap<>();

        Set<Map.Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

        for (Map.Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            int valLen = values.length;

            if (valLen == 1) {
                retMap.put(name, values[0]);
            } else if (valLen > 1) {
                StringBuilder sb = new StringBuilder();
                for (String val : values) {
                    sb.append(",").append(val);
                }
                retMap.put(name, sb.toString().substring(1));
            } else {
                retMap.put(name, "");
            }
        }

        return retMap;

    }
}
