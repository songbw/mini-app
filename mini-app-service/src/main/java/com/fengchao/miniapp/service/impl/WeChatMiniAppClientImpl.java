package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.config.MiniAppConfiguration;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.constant.PaymentStatusType;
import com.fengchao.miniapp.constant.WeChat;
import com.fengchao.miniapp.constant.WeChatErrorCode;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.service.IWechatMiniAppClient;
import com.fengchao.miniapp.utils.HttpClient431Util;
import com.fengchao.miniapp.utils.Md5Util;
import com.fengchao.miniapp.utils.RedisDAO;
import com.fengchao.miniapp.utils.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;
import sun.security.provider.MD5;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Service
public class WeChatMiniAppClientImpl implements IWechatMiniAppClient {

    private MiniAppConfiguration configuration;

    @Autowired
    public WeChatMiniAppClientImpl(MiniAppConfiguration configuration){

        this.configuration = configuration;

    }

    private String map2string(Map map) {
        StringBuilder sb = new StringBuilder();

        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        int i = 0;
        while (iter.hasNext()) {
            String key = iter.next();
            if (0 < i ) {
                sb.append("&");
                sb.append(key);
                sb.append("=");
                sb.append(map.get(key));
            } else {
                sb.append(key);
                sb.append("=");
                sb.append(map.get(key));
            }
            i++;
        }

        return sb.toString();

    }

    private JSONObject accessWeChatApi(String path,Map<String,String> params) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        String url = configuration.getWechatAppApiUrl() + path;
        String result;

        try{
            result = HttpClient431Util.doGet(params,url);
        }catch (Exception e){
            String msg = MyErrorCode.WECHAT_API_FAILED+e.getMessage();
            log.error(_func+msg);
            throw new Exception(msg);
        }

        if (null == result || result.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        JSONObject json;

        try {
            json = JSON.parseObject(result);
        }catch (Exception e){
            String msg = MyErrorCode.WECHAT_API_FAILED+e.getMessage();
            log.error(_func+msg);
            throw new Exception(msg);
        }

        if (null == json){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_WRONG;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        log.info("微信小程序服务端 返回JSON {}",JSON.toJSONString(json));

        return json;

    }

    private String buildRedundNo(){
        Long timeStampMs = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        Long timeStampS = timeStampMs/1000;
        String timeStamp = timeStampS.toString();
        Random random = new Random();

        String triRandom = random.nextInt(1000) + "";
        StringBuilder sb = new StringBuilder();
        int randLength = triRandom.length();
        if (randLength < 3) {
            for (int i = 1; i <= 3 - randLength; i++)
                sb.append("0");
        }
        sb.append(triRandom);
        return configuration.getWechatAppId() + timeStamp + sb.toString();
    }

    public String DecodePkcs7(String base64Str) throws Exception{
        //AES-256-ECB解密(PKCS7Padding)
        //解密步骤如下：
        //（1）对加密串A做base64解码，得到加密串B
        //（2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
        //
        //（3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
        String _func = "退款通知解密";
        log.info("{} 入参 {}",_func,base64Str);
        byte[] b;
        try {
            //
            b = new BASE64Decoder().decodeBuffer(base64Str);
        }catch (Exception e){
            log.error("Base64解码异常 {}",e.getMessage(),e);
            throw e;
        }

        String md5Key = Md5Util.md5(WeChat.MINI_APP_PAYMENT_API_KEY);
        byte[] key = md5Key.getBytes();

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        SecretKeySpec keySpec = new SecretKeySpec(key,"AES");
        cipher.init(Cipher.DECRYPT_MODE,keySpec);
        byte[] decoded = cipher.doFinal(b);

        String result = new String(decoded, StandardCharsets.UTF_8);
        log.info("{} 结果 {}",_func,result);
        return result;

    }

    @Override
    public WeChatTokenResultBean
    getAccessToken()
            throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        Map<String,String> params = new HashMap<>();
        params.put(WeChat.GRANT_TYPE_KEY,WeChat.GRANT_TYPE_DEFAULT);
        params.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        params.put(WeChat.SECRET_KEY,configuration.getWechatAppSecret());

        JSONObject json;
        try{
            json = accessWeChatApi(WeChat.GET_ACCESS_TOKEN_PATH,params);
        }catch (Exception e){
            throw e;
        }

        String token;
        Integer expires;
        try {
            token = json.getString(WeChat.ACCESS_TOKEN_KEY);
            expires = json.getInteger(WeChat.EXPIRES_IN_KEY);
        }catch (Exception e){
            String errMsg = MyErrorCode.COMMON_JSON_WRONG + json.getString(WeChat.ERR_MESSAGE_KEY);
            log.error(_func + errMsg);
            throw new Exception(errMsg);
        }
        if (null == token || token.isEmpty()) {
                //Integer errCode = json.getInteger(WeChat.ERR_CODE_KEY);
                String errMsg = MyErrorCode.WECHAT_API_FAILED + json.getString(WeChat.ERR_MESSAGE_KEY);
                log.error(_func + errMsg);
                throw new Exception(errMsg);
        }

        WeChatTokenResultBean bean = new WeChatTokenResultBean();
        bean.setAccess_token(token);
        bean.setExpires_in(expires);
        log.info(_func+WeChat.GET_ACCESS_TOKEN_API+JSON.toJSONString(bean));
        return bean;
    }

    @Override
    public WeChatSessionResultBean
    getSession(String jsCode)throws Exception {
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        Map<String, String> params = new HashMap<>();
        params.put(WeChat.APP_ID_KEY, configuration.getWechatAppId());
        params.put(WeChat.SECRET_KEY, configuration.getWechatAppSecret());
        params.put(WeChat.JS_CODE_KEY, jsCode);
        params.put(WeChat.GRANT_TYPE_KEY, WeChat.GRANT_TYPE_AUTH_CODE);

        JSONObject json;
        try {
            json = accessWeChatApi(WeChat.GET_CODE2SESSION_PATH, params);
        } catch (Exception e) {
            throw e;
        }

        String msg;

        String openId;
        String sessionKey;
        String unionId;
        try {
            openId = json.getString(WeChat.OPEN_ID_KEY);
            sessionKey = json.getString(WeChat.SESSION_KEY_KEY);
            unionId = json.getString(WeChat.UNION_ID_KEY);
        } catch (Exception e) {
            msg = MyErrorCode.COMMON_JSON_WRONG + e.getMessage();
            log.error(_func + msg);
            throw new Exception(msg);
        }
        if (null != openId && !openId.isEmpty() && null != sessionKey && !sessionKey.isEmpty()) {
            WeChatSessionResultBean bean = new WeChatSessionResultBean();
            bean.setOpenid(openId);
            bean.setSession_key(sessionKey);
            bean.setUnionid(unionId);

            log.info(_func + WeChat.GET_CODE2SESSION_PATH + JSON.toJSONString(bean));
            return bean;
        }

        String errMsg = null;
        Integer errCode = null;
        try {
            errMsg = json.getString(WeChat.ERR_MESSAGE_KEY);
            errCode = json.getInteger(WeChat.ERR_CODE_KEY);
        } catch (Exception e) {
            msg = MyErrorCode.COMMON_JSON_WRONG + e.getMessage();
            log.error(_func + msg);
        }
        if (null == errCode || null == errMsg) {
            msg = MyErrorCode.WECHAT_API_RESP_MSG_WRONG;
            log.error(_func + msg);
            throw new Exception(msg);
        }
        if (!WeChatErrorCode.SUCCESS.getCode().equals(errCode)) {
            String zhMsg = WeChatErrorCode.code2msg(errCode);
            if (zhMsg.isEmpty()) {
                zhMsg = errMsg;
            }
            msg = MyErrorCode.weChatErrMsg(String.valueOf(errCode), zhMsg);
            throw new Exception(msg);
        }

        msg = MyErrorCode.WECHAT_API_RESP_MSG_WRONG;
        log.error(_func + msg);
        throw new Exception(msg);

    }

    @Override
    public String
    signParam(Map<String,Object> params) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        Map<String,Object> treeMap = new TreeMap<>();

        Set<String> keySet = params.keySet();
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            treeMap.put(key, params.get(key));
        }

        String paramStr = map2string(treeMap);
        paramStr = paramStr + "&key="+WeChat.MINI_APP_PAYMENT_API_KEY;
        log.info("{} params: {}",_func,paramStr);
        try {
            String sign = Md5Util.md5(paramStr);
            log.info("{} md5: {}", _func,sign);
            return sign;
        }catch (Exception e){
            log.error("{} {}",_func,e.getMessage(),e);
            throw e;
        }

    }

    @Override
    public WechatPrepayBean
    postPrepayId(WechatOrderPostBean data,String ip) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} param: {}",_func,JSON.toJSONString(data));

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandom());
        paramMap.put(WeChat.BODY_KEY, data.getBody());
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, data.getTradeNo());
        paramMap.put(WeChat.TOTAL_FEE_KEY, data.getTotalFee());
        paramMap.put(WeChat.OPEN_ID_KEY, data.getOpenId());
        paramMap.put(WeChat.SPBILL_CREATE_IP_KEY,ip);
        paramMap.put(WeChat.NOTIFY_URL_KEY,WeChat.MINI_APP_PAYMENT_UNIFIED_ORDER_NOTIFY);
        paramMap.put(WeChat.TRADE_TYPE_KEY,WeChat.TRADE_TYPE_JSAPI);

        try {
            String sign = signParam(paramMap);
            paramMap.put(WeChat.SIGN_KEY,sign);
        }catch (Exception e){
            throw e;
        }

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",_func,postBody);

        String respXml;

        try{
            respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_PAYMENT_UNIFIED_ORDER_URL);
        }catch (Exception e){
            String msg = MyErrorCode.WECHAT_API_FAILED+e.getMessage();
            log.error(_func+msg);
            throw new Exception(msg);
        }

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        Map<String, Object> respMap = XmlUtil.xml2map(respXml);
        Object returnCodeObj = respMap.get(WeChat.RETURN_CODE_KEY);
        if (null == returnCodeObj){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.RETURN_CODE_KEY;
            log.error(_func+msg);
            throw new Exception(msg);
        }
        String returnCode = returnCodeObj.toString();
        if (returnCode.isEmpty() || !WeChat.RETURN_CODE_SUCCESS.equals(returnCode)){
            Object msgObj = respMap.get(WeChat.RETURN_MESSAGE_KEY);
            String msg;
            if (null == msgObj){
                msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            }else{
                msg = MyErrorCode.WECHAT_API_FAILED+msgObj.toString();
            }
            log.error(_func+msg);
            throw new Exception(msg);
        }

        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        if (null == resultCodeObj){
            String msg = MyErrorCode.WECHAT_API_RESULT_CODE_MISSING;
            log.error(_func+msg);
            throw new Exception(msg);
        }
        String resultCode = resultCodeObj.toString();
        if (resultCode.isEmpty() || !WeChat.RETURN_CODE_SUCCESS.equals(resultCode)){
            Object msgObj = respMap.get(WeChat.RESP_ERR_MESSAGE_KEY);
            String msg;
            if (null == msgObj){
                msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            }else{
                msg = MyErrorCode.WECHAT_API_FAILED+msgObj.toString();
            }
            log.error(_func+msg);
            throw new Exception(msg);
        }

        Object respAppIdObj = respMap.get(WeChat.APP_ID_KEY);
        if (null == respAppIdObj){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.APP_ID_KEY;
            log.error(_func+msg);
            throw new Exception(msg);
        }
        String respAppId = respAppIdObj.toString();
        if (respAppId.isEmpty() || !configuration.getWechatAppId().equals(respAppId)){
            String msg = MyErrorCode.WECHAT_API_RESP_APP_ID_WRONG;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        Object respNonceObj = respMap.get(WeChat.NONCE_STRING_KEY);
        Object respSignObj = respMap.get(WeChat.SPBILL_CREATE_IP_KEY);
        Object prepayIdObj = respMap.get(WeChat.PREPAY_ID_KEY);
        if (null == respNonceObj || null == respSignObj || null == prepayIdObj
              ||  respNonceObj.toString().isEmpty() || respSignObj.toString().isEmpty()
              ||  prepayIdObj.toString().isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.PREPAY_ID_KEY;
            log.error(_func+msg);
            throw new Exception(msg);
        }
        String respNonce = respNonceObj.toString();
        String respSing = respSignObj.toString();
        String prepayId = prepayIdObj.toString();

        Long timeStampMs = LocalDateTime.now().toInstant(ZoneOffset.MAX.of("+8")).toEpochMilli();
        Long timeStampS = timeStampMs/1000;
        String timeStamp = timeStampS.toString();

        WechatPrepayBean bean = new WechatPrepayBean();
        bean.setNonceStr(respNonce);
        bean.setPrepayId(prepayId);
        bean.setSign(respSing);
        bean.setTimeStamp(timeStamp);
        bean.setResultMsg(resultCode);

        return bean;
    }

    @Override
    public boolean isRightSign(String xmlStr) throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == xmlStr || xmlStr.isEmpty()){
            String msg = MyErrorCode.WECHAT_NOTIFY_SIGN_BLANK;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        Map<String,Object> map = XmlUtil.xml2map(xmlStr);
        Object signObj = map.get(WeChat.SIGN_KEY);
        if (null == signObj || signObj.toString().isEmpty()){
            String msg = MyErrorCode.WECHAT_NOTIFY_SIGN_BLANK;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        String respSign = signObj.toString();

        map.remove(WeChat.SIGN_KEY);
        try{
            String sign = signParam(map);
            if (sign.equals(respSign)){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            throw e;
        }

    }

    @Override
    public Refund
    postRefund(WechatRefundPostBean data) throws Exception{
        String _func = "微信小程序退款 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} param: {}",_func,JSON.toJSONString(data));

        String refundNo = buildRedundNo();
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandom());
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, data.getOrderId());
        paramMap.put(WeChat.OUT_REFUND_NO_KEY, refundNo);
        paramMap.put(WeChat.TOTAL_FEE_KEY, data.getTotalFee());
        paramMap.put(WeChat.REFUND_FEE_KEY, data.getRefundFee());
        paramMap.put(WeChat.NOTIFY_URL_KEY,WeChat.MINI_APP_REFUND_NOTIFY);

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",_func,postBody);

        String respXml;

        try{
            respXml = HttpClient431Util.doPostSSLXml(postBody,WeChat.MINI_APP_REFUND_URL);
        }catch (Exception e){
            String msg = MyErrorCode.WECHAT_API_FAILED+e.getMessage();
            throw new Exception(msg);
        }

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error(_func+msg);
            throw new Exception(msg);
        }

        Map<String, Object> respMap = XmlUtil.xml2map(respXml);
        Object returnCodeObj = respMap.get(WeChat.RETURN_CODE_KEY);
        if (null == returnCodeObj || returnCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.RETURN_CODE_KEY);
        }
        String returnCode = returnCodeObj.toString();
        Object msgObj = respMap.get(WeChat.RETURN_MESSAGE_KEY);
        String msg = MyErrorCode.WECHAT_API_FAILED;
        if (null != msgObj){
            msg += msgObj.toString();
        }
        if (!WeChat.RETURN_CODE_SUCCESS.equals(returnCode)){
            //接口调用出错
            throw new Exception(msg);
        }

        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        if (null == resultCodeObj || resultCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESULT_CODE_MISSING);
        }

        Refund refund = new Refund();
        BeanUtils.copyProperties(data,refund);
        refund.setCreateTime(new Date());
        refund.setUpdateTime(new Date());

        String resultCode = resultCodeObj.toString();

        if (!WeChat.RETURN_CODE_SUCCESS.equals(resultCode)){
            //业务失败
            refund.setStatus(resultCode);

            Object errCodeObj = respMap.get(WeChat.RESP_ERR_CODE_KEY);
            Object errMsgObj = respMap.get(WeChat.RESP_ERR_MESSAGE_KEY);
            String errMsg = " ";
            if (null != errCodeObj && !errCodeObj.toString().isEmpty()){
                errMsg += errCodeObj.toString();
            }
            if (null != errMsgObj && !errMsgObj.toString().isEmpty()){
                errMsg += " "+errMsgObj.toString();
            }

            refund.setComments(errMsg);

            log.info("{} 失败 {}",_func,msg);
            return refund;
        }
        refund.setStatus(resultCode);

        Object respRefundNoObj = respMap.get(WeChat.OUT_REFUND_NO_KEY);
        if (null == respRefundNoObj || respRefundNoObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.OUT_REFUND_NO_KEY);
        }
        String respRefundNo = respRefundNoObj.toString();
        refund.setWechatRefundNo(respRefundNo);

        Object respRefundFeeObj = respMap.get(WeChat.REFUND_FEE_KEY);
        if (null == respRefundFeeObj || respRefundFeeObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_REFUND_RESP_REFUND_FEE_BLANK);
        }else{
            refund.setRespRefundFee(Integer.valueOf(respRefundFeeObj.toString()));
        }

        Object cashFeeObj = respMap.get(WeChat.RESP_CASH_FEE_KEY);
        Object cashRefundFeeObj = respMap.get(WeChat.CASH_REFUND_FEE_KEY);
        if (null == cashFeeObj || null == cashFeeObj.toString() || cashFeeObj.toString().isEmpty()){
            log.error("{} 返回 缺失 {}",_func,WeChat.CASH_REFUND_FEE_KEY);
        }else {
            refund.setRefundCashFee(Integer.valueOf(cashFeeObj.toString()));
            if (null != cashRefundFeeObj && null != cashFeeObj.toString()) {
                refund.setRefundCashFee(Integer.valueOf(cashRefundFeeObj.toString()));
            }
        }

        Object tranIdObj = respMap.get(WeChat.RESP_TRANSACTION_ID_KEY);
        if (null == tranIdObj || null == tranIdObj.toString() || tranIdObj.toString().isEmpty()){
            log.error("{} 返回 缺失 {}",_func,WeChat.RESP_TRANSACTION_ID_KEY);
        }else {
            refund.setTransactionId(tranIdObj.toString());
        }

        return refund;
    }

    @Override
    public void
    queryRefund(Refund refund) throws Exception{
        String _func = "微信小程序退款查询 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} refundNo= {}",_func,refund.getRefundNo());

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandom());
        paramMap.put(WeChat.OUT_REFUND_NO_KEY, refund.getRefundNo());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",_func,postBody);

        String respXml;

        try{
            respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_REFUND_QUERY_URL);
        }catch (Exception e){
            throw new Exception(MyErrorCode.WECHAT_API_FAILED+e.getMessage());
        }

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            throw new Exception(msg);
        }

        Map<String, Object> respMap = XmlUtil.xml2map(respXml);
        Object returnCodeObj = respMap.get(WeChat.RETURN_CODE_KEY);
        if (null == returnCodeObj || returnCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.RETURN_CODE_KEY);
        }
        String returnCode = returnCodeObj.toString();
        Object msgObj = respMap.get(WeChat.RETURN_MESSAGE_KEY);
        String msg = MyErrorCode.WECHAT_API_FAILED;
        if (null != msgObj){
            msg += msgObj.toString();
        }
        if (!WeChat.RETURN_CODE_SUCCESS.equals(returnCode)){
            //接口调用出错
            throw new Exception(msg);
        }

        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        if (null == resultCodeObj || resultCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESULT_CODE_MISSING);
        }

        String resultCode = resultCodeObj.toString();

        if (!WeChat.RETURN_CODE_SUCCESS.equals(resultCode)){
            //业务失败
            refund.setStatus(resultCode);

            Object errCodeObj = respMap.get(WeChat.RESP_ERR_CODE_KEY);
            Object errMsgObj = respMap.get(WeChat.RESP_ERR_MESSAGE_KEY);
            String errMsg = " ";
            if (null != errCodeObj && !errCodeObj.toString().isEmpty()){
                errMsg += errCodeObj.toString();
            }
            if (null != errMsgObj && !errMsgObj.toString().isEmpty()){
                errMsg += " "+errMsgObj.toString();
            }

            refund.setComments(errMsg);

            log.info("{} {}",_func,msg);
            return;
        }

        Object respRefundNoObj = respMap.get(WeChat.OUT_REFUND_NO_KEY+"_0");
        if (null == respRefundNoObj || respRefundNoObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.OUT_REFUND_NO_KEY);
        }
        String respRefundNo = respRefundNoObj.toString();
        if (refund.getWechatRefundNo().isEmpty()) {
            refund.setWechatRefundNo(respRefundNo);
        }else{
            if (!respRefundNo.equals(refund.getWechatRefundNo())){
                throw new Exception(MyErrorCode.REFUND_NO_FOUND);
            }
        }

        Object respRefundFeeObj = respMap.get(WeChat.REFUND_FEE_KEY+"_0");
        if (null == respRefundFeeObj || respRefundFeeObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_REFUND_RESP_REFUND_FEE_BLANK);
        }else{
            refund.setRespRefundFee(Integer.valueOf(respRefundFeeObj.toString()));
        }

        Object refundStatusObj = respMap.get(WeChat.REFUND_STATUS_KEY+"_0");
        if (null == refundStatusObj || refundStatusObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_REFUND_STATUS_BLANK);
        }else{
            refund.setStatus(refundStatusObj.toString());
        }

        Object refundTimeObj = respMap.get(WeChat.REFUND_SUCCESS_TIME_KEY+"_0");
        if (null != refundTimeObj){
            refund.setSuccessTime(refundTimeObj.toString());
        }

        Object recvAccountObj = respMap.get(WeChat.RECV_ACCOUNT_KEY+"_0");
        if (null != recvAccountObj){
            refund.setRefundRecvAccount(recvAccountObj.toString());
        }
    }

    @Override
    public void
    queryPayment(Payment payment) throws Exception{
        String _func = "微信小程序查询订单 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} orderId= {}",_func,payment.getOrderId());

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandom());
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, payment.getOrderId());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",_func,postBody);

        String respXml;

        try{
            respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_PAYMENT_QUERY_URL);
        }catch (Exception e){
            throw new Exception(MyErrorCode.WECHAT_API_FAILED+e.getMessage());
        }

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            throw new Exception(msg);
        }

        Map<String, Object> respMap = XmlUtil.xml2map(respXml);
        Object returnCodeObj = respMap.get(WeChat.RETURN_CODE_KEY);
        if (null == returnCodeObj || returnCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.RETURN_CODE_KEY);
        }
        String returnCode = returnCodeObj.toString();
        Object msgObj = respMap.get(WeChat.RETURN_MESSAGE_KEY);
        String msg = MyErrorCode.WECHAT_API_FAILED;
        if (null != msgObj){
            msg += msgObj.toString();
        }
        if (!WeChat.RETURN_CODE_SUCCESS.equals(returnCode)){
            //接口调用出错
            throw new Exception(msg);
        }

        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        if (null == resultCodeObj || resultCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESULT_CODE_MISSING);
        }

        String resultCode = resultCodeObj.toString();

        if (!WeChat.RETURN_CODE_SUCCESS.equals(resultCode)){
            //业务失败
            throw new Exception(MyErrorCode.WECHAT_API_FAILED+" 查询失败");
        }

        Object respOrderObj = respMap.get(WeChat.OUT_TRADE_NO_KEY);
        if (null == respOrderObj || respOrderObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.OUT_TRADE_NO_KEY);
        }
        String respOrderId = respOrderObj.toString();
        if (!respOrderId.equals(payment.getOrderId())){
            throw new Exception(MyErrorCode.PAYMENT_OUT_TRAN_NO_WRONG);
        }

        String tradeState = " ";
        Object tradeStateObj = respMap.get(WeChat.TRADE_STATE_KEY);
        if (null == tradeStateObj || tradeStateObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_TRADE_STATUS_BLANK);
        }else{
            tradeState += tradeStateObj.toString();
        }
        Object tradeDescObj = respMap.get(WeChat.TRADE_STATE_DESC_KEY);
        if (null == tradeDescObj || tradeDescObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_TRADE_STATUS_BLANK);
        }else{
            tradeState += tradeDescObj.toString();
        }
        payment.setComments(tradeState);

        Object endTimeObj = respMap.get(WeChat.RESP_TIME_END_KEY);
        if (null == endTimeObj){
            log.error(MyErrorCode.WECHAT_TIME_END_BLANK);
        }else{
            payment.setTimeEnd(endTimeObj.toString());
        }

    }

    @Override
    public boolean
    closePayment(Payment payment) throws Exception{
        String _func = "微信小程序关闭订单 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} orderId= {}",_func,payment.getOrderId());

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandom());
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, payment.getOrderId());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",_func,postBody);

        String respXml;

        try{
            respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_PAYMENT_CLOSE_URL);
        }catch (Exception e){
            throw new Exception(MyErrorCode.WECHAT_API_FAILED+e.getMessage());
        }

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            throw new Exception(msg);
        }

        Map<String, Object> respMap = XmlUtil.xml2map(respXml);
        Object returnCodeObj = respMap.get(WeChat.RETURN_CODE_KEY);
        if (null == returnCodeObj || returnCodeObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.RETURN_CODE_KEY);
        }
        String returnCode = returnCodeObj.toString();
        Object msgObj = respMap.get(WeChat.RETURN_MESSAGE_KEY);
        String msg = MyErrorCode.WECHAT_API_FAILED;
        if (null != msgObj){
            msg += msgObj.toString();
        }
        if (!WeChat.RETURN_CODE_SUCCESS.equals(returnCode)){
            //接口调用出错
            throw new Exception(msg);
        }

        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        Object resultMsgObj = respMap.get(WeChat.RESULT_MESSAGE_KEY);
        if (null == resultCodeObj || resultCodeObj.toString().isEmpty()
            || null == resultMsgObj){
            throw new Exception(MyErrorCode.WECHAT_API_RESULT_CODE_MISSING);
        }

        String resultCode = resultCodeObj.toString();
        String resultMsg = resultMsgObj.toString();
        log.info("{} exit: code={} msg={}",_func,resultCode, resultMsg);
        if (!WeChat.RETURN_CODE_SUCCESS.equals(resultCode)){
            //业务失败
            throw new Exception(MyErrorCode.WECHAT_API_FAILED+resultMsg );
        }

        return true;
    }
}

