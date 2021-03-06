package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
// import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.constant.*;
import com.fengchao.miniapp.model.AliPayConfig;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.service.IAliPaySDKClient;


import java.net.URLEncoder;
import java.util.*;

import com.fengchao.miniapp.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class AliPaySDKClient implements IAliPaySDKClient {

    private AliPayConfigServiceImpl aliPayConfigService;
    private PaymentServiceImpl paymentService;
    private RefundServiceImpl refundService;

    @Autowired
    public AliPaySDKClient(PaymentServiceImpl paymentService,
                           RefundServiceImpl refundService,
                           AliPayConfigServiceImpl aliPayConfigService){

        this.aliPayConfigService = aliPayConfigService;
        this.paymentService = paymentService;
        this.refundService = refundService;
    }

    private Payment tryFindPaymentRecord(String tradeNo){

        PageInfo<Payment> pages = null ;
        int timeOut = 3;
        for(int i = 0; i < timeOut; i++) {

            try {
                pages = paymentService.queryList(1, 1, "id", "DESC",
                        null, tradeNo, null, null);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (null != pages && null != pages.getRows() && 0 < pages.getRows().size()) {
                return pages.getRows().get(0);
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        return null;
    }

    private Refund tryFindRefundRecord(String remoteRefundNo){

        Refund refund = null ;
        int timeOut = 3;
        for(int i = 0; i < timeOut; i++) {
            try {
                refund = refundService.getByRemoteRefundNo(remoteRefundNo);
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (null != refund) {
                return refund;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        return null;
    }

    @Override
    public AliPaySignParamBean
    signParam(Map<String,String> map, AliPayConfig config){
        String functionDescription = "?????????SDK??????: ";
        log.info("{} ?????? {}",functionDescription, JSON.toJSON(map));
        String content = AlipaySignature.getSignCheckContentV2(map);
        log.info("{} ???????????????????????? {}",functionDescription,content);

        String sign;
        try{
            sign = AlipaySignature.rsa256Sign(content, config.getPrivateKey(),AliPay.CHARSET);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
            return null;
        }

        AliPaySignParamBean bean = new AliPaySignParamBean();
        bean.setSign(sign);
        bean.setSignType(AliPay.SIGN_TYPE_VALUE);
        bean.setAppId(config.getiAppId());
        bean.setSignedRequestString(content);

        return bean;
    }

    @Override
    public String
    postWapPay(AliWapPayPostBean data, String iAppId){
        String functionDescription = "?????????????????????????????????: ";
        log.info("{} {}",functionDescription,JSON.toJSONString(data));

        AliPayConfig config = getAppIdConfig(iAppId);
        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        config.getPayAppId(),config.getPrivateKey(),
                        "json", AliPay.CHARSET,
                        config.getPublicKey(),
                        AliPay.SIGN_TYPE_VALUE);

        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setNotifyUrl(config.getPayNotify());
        if (null != data.getReturnUrl()) {
            alipayRequest.setReturnUrl(data.getReturnUrl());
        }
        Map<String,Object> map = new HashMap<>();
        map.put(AliPay.OUT_TRADE_NO_KEY,data.getTradeNo());
        map.put(AliPay.TOTAL_AMOUNT_KEY, Float.valueOf(FeeUtils.Fen2Yuan(data.getTotalAmount().toString())));
        map.put(AliPay.SUBJECT_KEY,data.getSubject());
        map.put(AliPay.PRODUCT_CODE_KEY,AliPay.PRODUCT_CODE_DEFAULT_VALUE);
        alipayRequest.setBizContent(JSON.toJSONString(map));

        try {
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            log.info(form);
            return form;
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException(MyErrorCode.ALIPAY_SDK_FAILED+e.getMessage());
        }
    }

    @Override
    public boolean
    verifySign(Map<String,String> map, String iAppId){
        String functionDescription = "?????????SDK??????: ";
        if (log.isDebugEnabled()) {
            log.debug("{} ?????? {}", functionDescription, JSON.toJSON(map));
        }
        boolean result;
        AliPayConfig config = getAppIdConfig(iAppId);
        try {
            //result = AlipaySignature.rsaCheckV2(map, AliPay.PUBLIC_KEY_VALUE, AliPay.CHARSET, AliPay.SIGN_TYPE_VALUE);
            result = AlipaySignature.rsaCheckV1(map, config.getPublicKey(), AliPay.CHARSET,AliPay.SIGN_TYPE_VALUE);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
            return false;
        }

        log.info("{} ????????? {}",functionDescription,result);
        return result;
    }

    @Override
    public WeChatTokenResultBean
    getToken(String code,String iAppId) throws Exception {
        String functionDescription = "???????????????token: ";
        log.info("{} ?????? {}",functionDescription,code);

        AliPayConfig config = getAppIdConfig(iAppId);
        Map<String,String> map = new HashMap<>();
        map.put(AliPay.GRANT_TYPE_KEY,AliPay.GRANT_TYPE_AUTH_CODE);
        map.put(AliPay.CODE_KEY,code);

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        config.getPayAppId(),config.getPrivateKey(),
                "json", AliPay.CHARSET,
                        config.getPublicKey(),
                        AliPay.SIGN_TYPE_VALUE);
        AlipayOpenAuthTokenAppRequest request = new AlipayOpenAuthTokenAppRequest();
        request.setBizContent(JSON.toJSONString(map));
        AlipayOpenAuthTokenAppResponse resp;

        try {
            resp = alipayClient.execute(request);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw e;
        }

        if (null == resp){
            log.error("{} ?????? token ??????",functionDescription);
            throw new Exception(MyErrorCode.ALIPAY_SDK_RESPONSE_NULL);
        }
        if (!resp.isSuccess()){
            log.error("{} ?????? token ?????? {}",functionDescription,resp.getMsg());
            throw new Exception(MyErrorCode.ALIPAY_SDK_GET_TOKEN_FAILED+resp.getMsg());
        }

        WeChatTokenResultBean bean = new WeChatTokenResultBean();
        bean.setAccess_token(resp.getAppAuthToken());
        bean.setExpires_in(Integer.valueOf(resp.getExpiresIn()));
        log.info("{} ??????token={} expires={}",functionDescription,resp.getAppAuthToken(), resp.getExpiresIn());
        return bean;
    }

    @Override
    public AliPayConfig getAppIdConfig(String iAppId){

        AliPayConfig config = aliPayConfigService.getByIAppId(iAppId);
        if (null != config && null != config.getPayAppId()){
            return config;
        }else{
            log.error("?????????iAppId={} ?????????????????????",iAppId);
            throw new RuntimeException(MyErrorCode.ALIPAY_CONFIG_BLANK);
        }

    }

    @Override
    public AliPayRefundRespBean
    refund(String orderId,Float amount,String iAppId) {
        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        String functionDescription = "???????????????SDK??????: ";
        log.info("{} ?????? orderId = {}",functionDescription,orderId);

        AliPayConfig config = getAppIdConfig(iAppId);
        String remoteRefundNo = RandomString.buildRefundNo(config.getPayAppId());
        Map<String,Object> map = new HashMap<>(3);
        map.put(AliPay.OUT_TRADE_NO_KEY,orderId);
        map.put(AliPay.REFUND_AMOUNT_KEY,amount);
        map.put(AliPay.OUT_REQUEST_NO_KEY, remoteRefundNo);

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        config.getPayAppId(),config.getPrivateKey(),
                        "json", AliPay.CHARSET,
                        config.getPublicKey(),
                        AliPay.SIGN_TYPE_VALUE);

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse resp;
        AliPayRefundRespBean bean = new AliPayRefundRespBean();
        bean.setRemoteRefundNo(remoteRefundNo);
        bean.setTradeNo(orderId);
        bean.setAliPayTradeNo(null);

        try{
            resp = alipayClient.execute(request);
        }catch (Exception ex){
            log.error("{} {}",functionDescription,ex.getMessage(),ex);
            bean.setComments("????????????SDK??????");
            return bean;
        }

        if (null == resp){
            log.error("{} ???????????????",functionDescription);
            bean.setComments("????????????SDK???????????????");
            return bean;
        }
        if (!resp.isSuccess()){
            log.error("{} ?????? {} {}",functionDescription,resp.getMsg(),resp.getSubMsg());
            bean.setComments("????????????SDK??????: "+resp.getMsg()+resp.getSubMsg());
            return bean;
        }

        log.info("{} SDK ?????? {}",functionDescription,JSON.toJSONString(resp));

        bean.setAliPayTradeNo(resp.getTradeNo());
        bean.setRefundFee(resp.getRefundFee());
        bean.setRefundDate(resp.getGmtRefundPay());
        bean.setAliPayLoginId(resp.getBuyerLogonId());
        bean.setBuyerId(resp.getBuyerUserId());

        log.info("{} ?????? {}",functionDescription, JSON.toJSONString(bean));
        return bean;
    }

    @Override
    public Payment handlePaymentNotify(Map<String,String> params){
        String functionDescription = "handlePaymentNotify: ";


        AliPayNotifyBean bean = JSON.parseObject(JSON.toJSONString(params),AliPayNotifyBean.class);
        log.info("?????????Bean {}",JSON.toJSONString(bean));

        String tradeNo = params.get(AliPay.OUT_TRADE_NO_KEY);
        String aliPayTradeNo = params.get(AliPay.TRADE_NO_KEY);
        String tradeStatue = params.get(AliPay.TRADE_STATUS_KEY);
        Payment record = tryFindPaymentRecord(tradeNo);

        if (!verifySign(params,record.getiAppId())){
            return null;
        }
        if (null == record){
            log.error("{} {}",functionDescription,MyErrorCode.PAYMENT_NO_FOUND);
            return null;
        }else{
            boolean isHandled =
                    null != record.getTransactionId() && !record.getTransactionId().isEmpty() &&
                    (PaymentStatusType.OK.getCode().equals(record.getStatus())|| PaymentStatusType.FAILED.getCode().equals(record.getStatus()));
            if (isHandled) {
                log.info("{} ????????????????????????,??????????????????", functionDescription);
                // ?????????????????????,??????????????????
                String remoteRefundNo = params.get(AliPay.OUT_BIZ_NO_KEY);
                if (null != remoteRefundNo){
                    log.info("????????????, remoteRefundNo={}",remoteRefundNo);
                    Refund refund = tryFindRefundRecord(remoteRefundNo);
                    if (null != refund){
                        refund.setComments(refund.getComments()+" ???????????????????????????");
                        try {
                            refundService.update(refund);
                        }catch (Exception e){
                            log.error("{} ????????????????????????",functionDescription);
                        }
                    }
                }
                record.setIp(MyErrorCode.NOTIFY_HANDLED_IP_DONE);//?????????????????????
                return record;
            }
        }

        record.setUpdateTime(new Date());
        record.setOrderId(tradeNo);
        record.setTransactionId(aliPayTradeNo);
        record.setResult(tradeStatue);

        if (null != tradeStatue){
            if( AliPay.TRADE_STATUS_OK.equals(tradeStatue)){
                record.setStatus(PaymentStatusType.OK.getCode());
                record.setComments("??????????????????,??????????????????");
            }else if (AliPay.TRADE_STATUS_CLOSE.equals(tradeStatue)){
                record.setStatus(PaymentStatusType.FAILED.getCode());
                record.setComments("??????????????????,????????????????????????????????????????????????????????????");
            }else if (AliPay.TRADE_STATUS_WAIT_PAY.equals(tradeStatue)){
                record.setStatus(PaymentStatusType.PREPAY.getCode());
                record.setComments("??????????????????,??????????????????");
            }else if (AliPay.TRADE_STATUS_FINISHED.equals(tradeStatue)){
                record.setStatus(PaymentStatusType.FAILED.getCode());
                record.setComments("??????????????????,????????????,????????????");
            } else {
                record.setStatus(PaymentStatusType.FAILED.getCode());
                record.setComments("??????????????????,?????????????????? "+tradeStatue);
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
        String endTime = params.get(AliPay.CLOSE_TIME_KEY);
        if (null != endTime && !endTime.isEmpty()){
            record.setTimeEnd(endTime);
        }else{
            endTime = params.get(AliPay.END_TIME_KEY);
            if (null != endTime && !endTime.isEmpty()) {
                record.setTimeEnd(endTime);
            }else{
                ///???????????????????????????????????????????????????????????????,??????????????????????????????
                String notifyTime = DateUtil.Date2String(new Date());
                record.setTimeEnd(notifyTime);
            }
        }
        String loginId = params.get(AliPay.BUYER_LOGON_ID_KEY);
        String buyerId = params.get(AliPay.BUYER_ID_KEY);
        if (null != buyerId) {
            record.setOpenId(buyerId);
        } else if (null != loginId){
            record.setOpenId(loginId);
        }

        String notifyId = params.get(AliPay.NOTIFY_ID_KEY);
        if (null != notifyId){
            record.setPrepayId(notifyId);
        }
        String fundList = params.get(AliPay.FUND_BILL_LIST_KEY);
        if (null != fundList){
            record.setBankType(JSON.toJSONString(fundList));
        }

        try{
            paymentService.update(record);
        }catch (Exception e){
            log.error("???????????????????????? {}",e.getMessage());
        }

        return record;
    }

    @Override
    public Refund
    queryRefund(Refund refund) {
        String functionDescription = "?????????SDK???????????? ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} ??????: {}", functionDescription, JSON.toJSONString(refund));

        String iAppId = refund.getiAppId();
        AliPayConfig config = getAppIdConfig(iAppId);
        Map<String, String> map = new HashMap<>();
        map.put(AliPay.OUT_TRADE_NO_KEY, refund.getOrderId());
        map.put(AliPay.OUT_REQUEST_NO_KEY, refund.getRemoteRefundNo());

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        config.getPayAppId(), config.getPrivateKey(),
                        "json", AliPay.CHARSET,
                        config.getPublicKey(),
                        AliPay.SIGN_TYPE_VALUE);

        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeFastpayRefundQueryResponse resp;
        try {
            resp = alipayClient.execute(request);
        } catch (Exception e) {
            log.error("{} {}", functionDescription, e.getMessage(), e);
            return refund;
        }

        log.info("{} SDK?????? {}", functionDescription, JSON.toJSONString(resp));
        String refundFee = resp.getRefundAmount();
        Long recordId = refund.getId();
        try {
            refund = refundService.getRecordById(recordId);
            if (null != refund && RefundStatusEnum.PENDING.getCode().equals(refund.getStatus())) {
                if (null != refundFee) {
                    refund.setRefundFee(Integer.valueOf(FeeUtils.Yuan2Fen(refundFee)));
                }
                if (resp.isSuccess()) {
                    refund.setComments(AliPay.REFUND_OK_COMMENTS);
                    refund.setStatus(RefundStatusEnum.SUCCESS.getCode());
                }else{
                    refund.setComments(resp.getSubCode()+resp.getSubMsg());
                    refund.setStatus(RefundStatusEnum.FAILED.getCode());
                }
                refund.setUpdateTime(new Date());
                refundService.update(refund);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return refund;
        }
        log.info("{} ?????????????????? {}", functionDescription, JSON.toJSONString(refund));

        return refund;
    }

    @Override
    public Payment
    queryPayment(Payment record){

        // Thread.currentThread().getStackTrace()[1].getMethodName();
        String functionDescription = "????????????????????? ";
        log.info("{} ?????? {}",functionDescription, JSON.toJSONString(record));

        String iAppId = record.getiAppId();
        AliPayConfig config = getAppIdConfig(iAppId);
        Map<String,String> map = new HashMap<>();
        map.put(AliPay.OUT_TRADE_NO_KEY,record.getOrderId());

        AlipayClient alipayClient =
                new DefaultAlipayClient(AliPay.GATEWAY_URL,
                        config.getPayAppId(),config.getPrivateKey(),
                        "json", AliPay.CHARSET,
                        config.getPublicKey(),
                        AliPay.SIGN_TYPE_VALUE);

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeQueryResponse resp;
        try {
            resp = alipayClient.execute(request);
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw new RuntimeException(MyErrorCode.ALIPAY_SDK_FAILED+" ??????????????????");
        }

        log.info("{} SDK ?????? {}",functionDescription,JSON.toJSONString(resp));

        String tradeStatue = resp.getTradeStatus();
        Integer newStatus ;
        String newComments;
        if (null != tradeStatue){
            if( AliPay.TRADE_STATUS_OK.equals(tradeStatue)){
                newStatus = PaymentStatusType.OK.getCode();
                newComments = "??????????????????,??????????????????";
            }else if (AliPay.TRADE_STATUS_CLOSE.equals(tradeStatue)){
                newStatus = PaymentStatusType.CLOSED.getCode();
                newComments = "??????????????????,????????????????????????????????????????????????????????????";
            }else if (AliPay.TRADE_STATUS_WAIT_PAY.equals(tradeStatue)){
                newStatus = PaymentStatusType.PREPAY.getCode();
                newComments = "??????????????????,??????????????????";
            }else if (AliPay.TRADE_STATUS_FINISHED.equals(tradeStatue)){
                newStatus = PaymentStatusType.FAILED.getCode();
                newComments = "??????????????????,????????????,????????????";
            } else {
                newStatus = PaymentStatusType.FAILED.getCode();
                newComments = "??????????????????,?????????????????? "+tradeStatue;
            }

            if (!newStatus.equals(record.getStatus())){
                record.setResult(resp.getTradeStatus());
                if (null != resp.getBuyerUserId()) {
                    record.setOpenId(resp.getBuyerUserId());
                }
                if (null != resp.getFundBillList()){
                    record.setBankType(JSON.toJSONString(resp.getFundBillList()));
                }
                record.setStatus(newStatus);
                record.setComments(newComments);
                record.setUpdateTime(new Date());

                Long recordId = record.getId();
                try {
                    Payment payment = paymentService.getRecordById(recordId);
                    if (null != payment &&
                            (PaymentStatusType.PREPAY.getCode().equals(payment.getStatus()) ||
                                    PaymentStatusType.PREPAY.getCode().equals(payment.getStatus()))){
                        paymentService.update(record);
                    }
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                    return record;
                }
                log.info("?????????????????? {}",JSON.toJSONString(record));
            }
        }

        return record;
    }

    @Override
    public Map<String,String>
    request2Map(HttpServletRequest request){

        Map<String,String> params = new HashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //??????????????????????????????????????????????????????
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        if (log.isDebugEnabled()) {
            log.debug("???request?????????????????????Map {}", JSON.toJSONString(params));
        }
        return params;

    }

    @Override
    public String
    buildJSSDKPayString(String iAppId,String outTradeNo,String totalAmount,String subject,String returnUrl){
        String function = "???????????????JSSDK????????????";
        log.info("{} iAppId={}, outTradeNo={}, totalAmount={}, subject={}",
                function,iAppId,outTradeNo,totalAmount,subject);

        AliPayConfig config = getAppIdConfig(iAppId);

        Map<String,Object> bizContentMap = new TreeMap<>();
        bizContentMap.put(AliPay.OUT_TRADE_NO_KEY,outTradeNo.trim());
        bizContentMap.put(AliPay.TOTAL_AMOUNT_KEY, FeeUtils.Fen2Yuan(totalAmount).trim());
        bizContentMap.put(AliPay.SUBJECT_KEY,subject.trim());
        bizContentMap.put(AliPay.PRODUCT_CODE_KEY,AliPay.PRODUCT_CODE_APP_PAY);
        ///??????????????????????????????????????? 120??????
        bizContentMap.put(AliPay.TIMEOUT_EXPRESS_KEY,"120m");

        Map<String,String> map = new HashMap<>();
        map.put(AliPay.APP_ID_KEY,config.getPayAppId());
        map.put(AliPay.METHOD_KEY,AliPay.METHOD_PAY);
        map.put(AliPay.CHARSET_KEY, AliPay.CHARSET_UTF8);
        map.put(AliPay.SIGN_TYPE_KEY,AliPay.SIGN_TYPE_VALUE);
        map.put(AliPay.TIMESTAMP_KEY,DateUtil.Date2String(new Date()));
        map.put(AliPay.VERSION_KEY,"1.0");
        map.put(AliPay.NOTIFY_URL_KEY,config.getPayNotify());
        if (null != returnUrl) {
            map.put(AliPay.RETURN_URL_KEY, returnUrl);
        }
        map.put(AliPay.BIZ_CONTENT_KEY,JSON.toJSONString(bizContentMap));


        AliPaySignParamBean signParamBean = signParam(map,config);
        map.put(AliPay.SIGN_KEY,signParamBean.getSign());
        return urlEncodeValues(map);

    }

    private String urlEncodeValues(Map<String, String> params){
        String functionDescription = "app????????????url??????: ";
        log.info("{} ?????? {}",functionDescription, JSON.toJSON(params));

        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        try {
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                String value = URLEncoder.encode(params.get(key), AliPay.CHARSET_UTF8);
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
            }

            log.info("{} ????????????????????? {}", functionDescription, content);
            return content.toString();
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException(MyErrorCode.ALIPAY_SDK_FAILED+e.getMessage());
        }
    }
}
