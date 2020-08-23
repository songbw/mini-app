package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fengchao.miniapp.bean.*;
import com.fengchao.miniapp.config.MiniAppConfiguration;
import com.fengchao.miniapp.config.RenterConfig;
import com.fengchao.miniapp.constant.*;
import com.fengchao.miniapp.dto.VendorWechatConfig;
import com.fengchao.miniapp.model.Payment;
import com.fengchao.miniapp.model.Refund;
import com.fengchao.miniapp.service.IWechatMiniAppClient;
import com.fengchao.miniapp.utils.HttpClient431Util;
import com.fengchao.miniapp.utils.Md5Util;
import com.fengchao.miniapp.utils.RandomString;
import com.fengchao.miniapp.utils.XmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class WeChatMiniAppClientImpl implements IWechatMiniAppClient {

    private MiniAppConfiguration configuration;
    private RenterConfig renterConfig;

    static{
        try{
            Security.addProvider(new BouncyCastleProvider());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Autowired
    public WeChatMiniAppClientImpl(MiniAppConfiguration configuration,RenterConfig renterConfig){

        this.configuration = configuration;
        this.renterConfig = renterConfig;
    }

    private String map2string(Map<String,Object> map) {
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

    private JSONObject accessMiniApiToken(String path,Map<String,String> params, String iAppId){
        String functionDescription = "获取accessToken接口 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        if (log.isDebugEnabled()) {
            log.info("{} 参数 {}", functionDescription, JSON.toJSON(params));
        }
        AllConfigurationBean config = getConfig(iAppId);
        if (null == config){
            String msg = MyErrorCode.I_APP_ID_INVALID;
            throw new RuntimeException(msg);
        }
        if (null == config.getMiniAppApiUrl()){
            String msg = MyErrorCode.CONFIG_MISSING+ " 微信API url";
            log.error("{} {}",functionDescription,msg);
            throw new RuntimeException(msg);
        }
        String url = config.getMiniAppApiUrl() + path;
        String result;

        try{
            result = HttpClient431Util.doGet(params,url);
        }catch (Exception e){
            String msg = MyErrorCode.WECHAT_API_FAILED+e.getMessage();
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw new RuntimeException(msg);
        }

        if (null == result || result.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error("{} {}",functionDescription,msg);
            throw new RuntimeException(msg);
        }

        JSONObject json;

        try {
            json = JSON.parseObject(result);
        }catch (Exception e){
            String msg = MyErrorCode.WECHAT_API_FAILED+e.getMessage();
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw new RuntimeException(msg);
        }

        if (null == json){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_WRONG;
            log.error("{} {}",functionDescription,msg);
            throw new RuntimeException(msg);
        }

        log.info("{} 返回 {}",functionDescription,JSON.toJSONString(json));

        return json;

    }

    public String decodePKCS7(String base64Str) throws Exception{
        //AES-256-ECB解密(PKCS7Padding)
        //解密步骤如下：
        //（1）对加密串A做base64解码，得到加密串B
        //（2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )
        //
        //（3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
        String functionDescription = " Pkcs7解密";
        log.info("{} 入参 {}",functionDescription,base64Str);
        byte[] b;
        try {
            //
            b = new BASE64Decoder().decodeBuffer(base64Str);
        }catch (Exception e){
            log.error("{} Base64解码异常 {}",functionDescription,e.getMessage(),e);
            throw new Exception(MyErrorCode.COMMON_AES256_FAILED+e.getMessage());
        }

        try {
            String md5Key =  DigestUtils.md5DigestAsHex(WeChat.MINI_APP_PAYMENT_API_KEY.getBytes(StandardCharsets.UTF_8));
            byte[] key = md5Key.getBytes();

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            //Cipher cipher = new Cipher("AES/ECB/PKCS7Padding", "BC");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = cipher.doFinal(b);

            String result = new String(decoded, StandardCharsets.UTF_8);

            log.info("{} 结果 {}", functionDescription, result);
            return result;
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
            return null;
        }

    }

    @Override
    public String
    getAppId(String apiType, String iAppId) throws Exception{

        AllConfigurationBean config = getConfig(iAppId);
        if (null == config){
            String msg = MyErrorCode.I_APP_ID_INVALID;
            throw new Exception(msg);
        }
        if (null == config.getJsAPIAppId() || null == config.getMiniAppId()){
            String msg = MyErrorCode.CONFIG_MISSING+ " 微信API url";
            log.error("getApId {}",msg);
            throw new Exception(msg);
        }

        if (ApiType.MINI.getCode().equals(apiType)){
            return config.getMiniAppId();
        }else {
            return config.getJsAPIAppId();
        }
    }

    //聚合支付中标记的支付类型, 凤巢微信小程序支付:fcwxxcx  凤巢微信公众号支付:fcwx  凤巢微信H5支付:fcwxh5
    @Override
    public String getPayType(String apiType){

        if (ApiType.MINI.getCode().equals(apiType)){
            return "fcwxxcx";
        }else {
            return "fcwx";
        }
    }

    @Override
    public WeChatTokenResultBean
    getAccessToken(String apiType,String iAppId)
            throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();

        AllConfigurationBean config = getConfig(iAppId);
        if (null == config){
            String msg = MyErrorCode.I_APP_ID_INVALID;
            throw new Exception(msg);
        }

        Map<String,String> params = buildParams(apiType,config);
        params.put(WeChat.GRANT_TYPE_KEY,WeChat.GRANT_TYPE_DEFAULT);

        JSONObject json;
        try{
            json = accessMiniApiToken(WeChat.GET_ACCESS_TOKEN_PATH,params,iAppId);
        }catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }

        String token;
        Integer expires;
        try {
            token = json.getString(WeChat.ACCESS_TOKEN_KEY);
            expires = json.getInteger(WeChat.EXPIRES_IN_KEY);
        }catch (Exception e){
            String errMsg = MyErrorCode.COMMON_JSON_WRONG + json.getString(WeChat.ERR_MESSAGE_KEY);
            log.error("{} {}",functionDescription,errMsg);
            throw new Exception(errMsg);
        }
        if (null == token || token.isEmpty()) {
                //Integer errCode = json.getInteger(WeChat.ERR_CODE_KEY);
                String errMsg = MyErrorCode.WECHAT_API_FAILED + json.getString(WeChat.ERR_MESSAGE_KEY);
                log.error("{} {}",functionDescription,errMsg);
                throw new Exception(errMsg);
        }

        WeChatTokenResultBean bean = new WeChatTokenResultBean();
        bean.setAccess_token(token);
        bean.setExpires_in(expires);
        log.info("{} {}",functionDescription,WeChat.GET_ACCESS_TOKEN_API+JSON.toJSONString(bean));

        return bean;
    }

    @Override
    public WeChatSessionResultBean
    getSession(String jsCode, String apiType, String iAppId)throws Exception {
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (log.isDebugEnabled()) {
            log.info("{} 参数： jsCode={}, iAppId={}", functionDescription, jsCode,iAppId);
        }
        AllConfigurationBean config = getConfig(iAppId);
        if (null == config){
            String msg = MyErrorCode.I_APP_ID_INVALID;
            throw new Exception(msg);
        }

        Map<String, String> params = buildParams(apiType,config);
        params.put(WeChat.JS_CODE_KEY, jsCode);
        params.put(WeChat.GRANT_TYPE_KEY, WeChat.GRANT_TYPE_AUTH_CODE);

        JSONObject json = accessMiniApiToken(WeChat.GET_CODE2SESSION_PATH, params,iAppId);

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
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw new Exception(msg);
        }
        if (null != openId && !openId.isEmpty() && null != sessionKey && !sessionKey.isEmpty()) {
            WeChatSessionResultBean bean = new WeChatSessionResultBean();
            bean.setOpenid(openId);
            bean.setSession_key(sessionKey);
            bean.setUnionid(unionId);
            if (log.isDebugEnabled()) {
                log.info("{} 成功 {}", functionDescription, JSON.toJSONString(bean));
            }
            return bean;
        }

        String errMsg = null;
        Integer errCode = null;
        try {
            errMsg = json.getString(WeChat.ERR_MESSAGE_KEY);
            errCode = json.getInteger(WeChat.ERR_CODE_KEY);
        } catch (Exception e) {
            msg = MyErrorCode.COMMON_JSON_WRONG + e.getMessage();
            log.error("{} {}",functionDescription,msg);
        }
        if (null == errCode || null == errMsg) {
            msg = MyErrorCode.WECHAT_API_RESP_MSG_WRONG;
            log.error("{} {}",functionDescription,msg);
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
        log.error("{} {}",functionDescription,msg);
        throw new Exception(msg);

    }

    @Override
    public String
    signParam(Map<String,Object> params){
        String functionDescription = "签名 ";//Thread.currentThread().getStackTrace()[1].getMethodName();

        if (log.isDebugEnabled()) {
            log.info("{} 参数 {}", functionDescription, JSON.toJSONString(params));
        }
        Map<String,Object> treeMap = new TreeMap<>();

        Set<String> keySet = params.keySet();
        /*
        Iterator<String> iter = keySet.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            treeMap.put(key, params.get(key));
        }
        */
        keySet.forEach(key->treeMap.put(key,params.get(key))
        );


        String paramStr = map2string(treeMap);
        paramStr = paramStr + "&key="+WeChat.MINI_APP_PAYMENT_API_KEY;
        if (log.isDebugEnabled()) {
            log.info("{} 参数串: {}", functionDescription, paramStr);
        }
        try {
            String sign = Md5Util.md5(paramStr);
            if (log.isDebugEnabled()) {
                log.info("{} md5: {}", functionDescription, sign);
            }
            return sign;
        }catch (Exception e){
            log.error("{} {}",functionDescription,e.getMessage(),e);
            throw new RuntimeException(MyErrorCode.COMMON_MD5_FAILED+e.getMessage());
        }

    }

    @Override
    public WechatPrepayBean
    postPrepayId(WechatOrderPostBean data,String ip, String apiType,String iAppId) throws Exception {
        String functionDescription = "统一下单接口 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        if (log.isDebugEnabled()) {
            log.debug("{} param: {}, iAppId={}", functionDescription, JSON.toJSONString(data), iAppId);
        }
        AllConfigurationBean config = getConfig(iAppId);
        if (null == config) {
            String msg = MyErrorCode.I_APP_ID_INVALID;
            throw new Exception(msg);
        }
        String appId = getAppId(apiType, iAppId);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY, appId);
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandomStringByLength(32));
        paramMap.put(WeChat.BODY_KEY, data.getBody());
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, data.getTradeNo());
        paramMap.put(WeChat.TOTAL_FEE_KEY, data.getTotalFee());
        paramMap.put(WeChat.OPEN_ID_KEY, data.getOpenId());
        paramMap.put(WeChat.SPBILL_CREATE_IP_KEY, ip);
        paramMap.put(WeChat.NOTIFY_URL_KEY, config.getPayNotify());
        paramMap.put(WeChat.TRADE_TYPE_KEY, WeChat.TRADE_TYPE_JSAPI);

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY, sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}", functionDescription, postBody);

        String respXml = HttpClient431Util.doPostXml(postBody, WeChat.MINI_APP_PAYMENT_UNIFIED_ORDER_URL);
        if (null == respXml || respXml.isEmpty()) {
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }

        Map<String, Object> respMap = XmlUtil.xml2map(respXml);
        Object returnCodeObj = respMap.get(WeChat.RETURN_CODE_KEY);
        if (null == returnCodeObj) {
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING + WeChat.RETURN_CODE_KEY;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }
        String returnCode = returnCodeObj.toString();
        if (returnCode.isEmpty() || !WeChat.RETURN_CODE_SUCCESS.equals(returnCode)) {
            Object msgObj = respMap.get(WeChat.RETURN_MESSAGE_KEY);
            String msg;
            if (null == msgObj) {
                msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            } else {
                msg = MyErrorCode.WECHAT_API_FAILED + msgObj.toString();
            }
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }

        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        if (null == resultCodeObj) {
            String msg = MyErrorCode.WECHAT_API_RESULT_CODE_MISSING;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }
        String resultCode = resultCodeObj.toString();
        if (resultCode.isEmpty() || !WeChat.RETURN_CODE_SUCCESS.equals(resultCode)) {
            Object msgObj = respMap.get(WeChat.RESP_ERR_MESSAGE_KEY);
            Object errCodeObj = respMap.get(WeChat.RESP_ERR_CODE_KEY);
            String msg;
            String errCode;
            if (null != errCodeObj) {
                errCode = errCodeObj.toString() + " ";
            } else {
                errCode = "";
            }
            if (null == msgObj) {
                msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            } else {
                msg = MyErrorCode.WECHAT_API_FAILED + errCode + msgObj.toString();
            }
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }

        Object respAppIdObj = respMap.get(WeChat.APP_ID_KEY);
        if (null == respAppIdObj) {
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING + WeChat.APP_ID_KEY;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }
        String respAppId = respAppIdObj.toString();
        if (respAppId.isEmpty() || !appId.equals(respAppId)) {
            String msg = MyErrorCode.WECHAT_API_RESP_APP_ID_WRONG;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }

        Object respNonceObj = respMap.get(WeChat.NONCE_STRING_KEY);
        Object respSignObj = respMap.get(WeChat.SIGN_KEY);
        Object prepayIdObj = respMap.get(WeChat.PREPAY_ID_KEY);
        if (null == prepayIdObj || prepayIdObj.toString().isEmpty()) {
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING + WeChat.PREPAY_ID_KEY;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }

        if (null == respNonceObj || respNonceObj.toString().isEmpty()) {
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING + WeChat.NONCE_STRING_KEY;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }

        if (null == respSignObj || respSignObj.toString().isEmpty()) {
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING + WeChat.SIGN_KEY;
            log.error("{} {}", functionDescription, msg);
            throw new Exception(msg);
        }
        String respNonce = respNonceObj.toString();
        ///String respSing = respSignObj.toString();
        String prepayId = prepayIdObj.toString();

        //Long timeStampMs = LocalDateTime.now().toInstant(ZoneOffset.MAX.of("+8")).toEpochMilli();
        Long timeStampMs = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Long timeStampS = timeStampMs / 1000;
        String timeStamp = timeStampS.toString();

        Map<String, Object> beanMap = new HashMap<>();
        String packageStr = WeChat.PREPAY_ID_KEY + "=" + prepayId;
        beanMap.put("appId", appId);
        beanMap.put("nonceStr", respNonce);
        beanMap.put("package", packageStr);
        beanMap.put("signType", WeChat.SIGN_TYPE_MD5);
        beanMap.put("timeStamp", timeStamp);

        WechatPrepayBean bean = new WechatPrepayBean();

        try {
            String paySign = signParam(beanMap);
            bean.setPaySign(paySign);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        bean.setNonceStr(respNonce);
        bean.setPackageStr(packageStr);
        bean.setTimeStamp(timeStamp);
        bean.setSignType(WeChat.SIGN_TYPE_MD5);

        bean.setPrepayId(prepayId);
        bean.setResult(resultCode);

        return bean;
    }

    @Override
    public Map<String,Object> verifySign(String xmlStr) throws Exception{
        String functionDescription = Thread.currentThread().getStackTrace()[1].getMethodName();
        if (null == xmlStr || xmlStr.isEmpty()){
            String msg = MyErrorCode.WECHAT_NOTIFY_SIGN_BLANK;
            log.error("{} {}",functionDescription,msg);
            throw new Exception(msg);
        }

        Map<String,Object> map = XmlUtil.xml2map(xmlStr);
        Object signObj = map.get(WeChat.SIGN_KEY);
        if (null == signObj || signObj.toString().isEmpty()){
            String msg = MyErrorCode.WECHAT_NOTIFY_SIGN_BLANK;
            log.error("{} {}",functionDescription,msg);
            throw new Exception(msg);
        }

        String respSign = signObj.toString();

        map.remove(WeChat.SIGN_KEY);
        try{
            String sign = signParam(map);
            if (sign.equals(respSign)){
                return map;
            }else{
                return null;
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }

    }

    @Override
    public Refund
    postRefund(Refund refund,WechatRefundPostBean data, String apiType,String iAppId) throws Exception{
        String functionDescription = "微信支付退款接口 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} param: {}",functionDescription,JSON.toJSONString(data));

        String appId = getAppId(apiType,iAppId);
        AllConfigurationBean config = getConfig(iAppId);
        if (null == config){
            String msg = MyErrorCode.I_APP_ID_INVALID;
            throw new Exception(msg);
        }

        String forWxRefundNo = RandomString.buildRefundNo(appId);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,appId);
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandomStringByLength(32));
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, data.getOrderId());
        paramMap.put(WeChat.OUT_REFUND_NO_KEY, forWxRefundNo);
        paramMap.put(WeChat.TOTAL_FEE_KEY, data.getTotalFee());
        paramMap.put(WeChat.REFUND_FEE_KEY, data.getRefundFee());
        paramMap.put(WeChat.NOTIFY_URL_KEY, config.getRefundNotify());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",functionDescription,postBody);

        String respXml;
        try{
            respXml = HttpClient431Util.doPostSSLXml(postBody,WeChat.MINI_APP_REFUND_URL);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error("{} {}",functionDescription,msg);log.error(functionDescription+msg);
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

        refund.setRemoteRefundNo(forWxRefundNo);

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

            log.info("{} 失败 {}",functionDescription,errMsg);
            return refund;
        }
        refund.setStatus(resultCode);
        refund.setComments("申请退款成功");

        Object respRefundNoObj = respMap.get(WeChat.OUT_REFUND_NO_KEY);
        if (null == respRefundNoObj || respRefundNoObj.toString().isEmpty()){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.OUT_REFUND_NO_KEY);
        }
        String respRefundNo = respRefundNoObj.toString();
        refund.setRemoteRefundNo(respRefundNo);

        Object respRefundFeeObj = respMap.get(WeChat.REFUND_FEE_KEY);
        if (null == respRefundFeeObj || respRefundFeeObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_REFUND_RESP_REFUND_FEE_BLANK);
        }else{
            refund.setRespRefundFee(Integer.valueOf(respRefundFeeObj.toString()));
        }

        Object cashFeeObj = respMap.get(WeChat.RESP_CASH_FEE_KEY);
        Object cashRefundFeeObj = respMap.get(WeChat.CASH_REFUND_FEE_KEY);
        if (null == cashFeeObj || null == cashFeeObj.toString() || cashFeeObj.toString().isEmpty()){
            log.error("{} 返回 缺失 {}",functionDescription,WeChat.CASH_REFUND_FEE_KEY);
        }else {
            refund.setCashFee(Integer.valueOf(cashFeeObj.toString()));
            if (null != cashRefundFeeObj && null != cashFeeObj.toString()) {
                refund.setRefundCashFee(Integer.valueOf(cashRefundFeeObj.toString()));
            }
        }

        Object tranIdObj = respMap.get(WeChat.RESP_TRANSACTION_ID_KEY);
        if (null == tranIdObj || null == tranIdObj.toString() || tranIdObj.toString().isEmpty()){
            log.error("{} 返回 缺失 {}",functionDescription,WeChat.RESP_TRANSACTION_ID_KEY);
        }else {
            refund.setTransactionId(tranIdObj.toString());
        }

        return refund;
    }

    @Override
    public void
    queryRefund(WechatRefundListBean refund,String apiType,String iAppId) throws Exception{
        ///Thread.currentThread().getStackTrace()[1].getMethodName();
        String functionDescription = "微信支付退款查询 ";
        log.info("{} orderId= {}",functionDescription,refund.getOrderId());

        String appId = getAppId(apiType,iAppId);

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,appId);
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandomStringByLength(32));
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, refund.getOrderId());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",functionDescription,postBody);

        String respXml;

        respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_REFUND_QUERY_URL);

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

        checkResultCode(respMap);

        Object refundCountObj = respMap.get(WeChat.REFUND_COUNT_KEY);
        if (null == refundCountObj){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.REFUND_COUNT_KEY);
        }

        Integer refundCount = Integer.valueOf(refundCountObj.toString());
        refund.setRefundCount(refundCount);

        List<WechatRefundDetailBean> list = new ArrayList<>();
        refund.setList(list);
        int i;
        for(i = 0; i < refundCount; i++) {
            WechatRefundDetailBean bean = new WechatRefundDetailBean();
            String num = "_"+String.valueOf(i);

            Object respRefundNoObj = respMap.get(WeChat.OUT_REFUND_NO_KEY + num);
            if (null != respRefundNoObj) {
                bean.setRefundNo(respRefundNoObj.toString());
            }

            Object wxRefundId = respMap.get(WeChat.REFUND_ID_KEY + num);
            if (null != wxRefundId) {
                bean.setWxRefundNo(wxRefundId.toString());
            }

            Object respRefundFeeObj = respMap.get(WeChat.REFUND_FEE_KEY + num);
            if (null != respRefundFeeObj && !respRefundFeeObj.toString().isEmpty()) {
                bean.setRefundFee(Integer.valueOf(respRefundFeeObj.toString()));
            }

            Object refundStatusObj = respMap.get(WeChat.REFUND_STATUS_KEY + num);
            if (null != refundStatusObj) {
                bean.setRefundStatus(refundStatusObj.toString());
            }

            Object refundTimeObj = respMap.get(WeChat.REFUND_SUCCESS_TIME_KEY + num);
            if (null != refundTimeObj) {
                bean.setRefundTime(refundTimeObj.toString());
            }

            Object recvAccountObj = respMap.get(WeChat.RECV_ACCOUNT_KEY + num);
            if (null != recvAccountObj) {
                bean.setRecvAccount(recvAccountObj.toString());
            }

            list.add(bean);
        }
    }

    @Override
    public void
    queryRefundStatus(Refund refund, String apiType,String iAppId) throws Exception{
        ///Thread.currentThread().getStackTrace()[1].getMethodName();
        String functionDescription = "微信小程序退款状态查询 ";
        if (log.isDebugEnabled()) {
            log.info("{} enter: {}", functionDescription, JSON.toJSONString(refund));
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,getAppId(apiType,iAppId));
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandomStringByLength(32));
        paramMap.put(WeChat.OUT_REFUND_NO_KEY, refund.getRefundNo());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        if (log.isDebugEnabled()) {
            log.info("{} post body : {}", functionDescription, postBody);
        }
        String respXml;

        respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_REFUND_QUERY_URL);

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
        checkResultCode(respMap);

        Object refundCountObj = respMap.get(WeChat.REFUND_COUNT_KEY);
        if (null == refundCountObj){
            throw new Exception(MyErrorCode.WECHAT_API_RESP_MSG_MISSING+ WeChat.REFUND_COUNT_KEY);
        }

        if (1 != Integer.valueOf(refundCountObj.toString())){
            log.error("{} 微信查询异常： 一个退款单号对应多个退款记录");
        }

        int i;
        for(i = 0; i < 1; i++) {
            ///WechatRefundDetailBean bean = new WechatRefundDetailBean();
            String num = "_"+String.valueOf(i);

            Object respRefundFeeObj = respMap.get(WeChat.REFUND_FEE_KEY + num);
            if (null != respRefundFeeObj && !respRefundFeeObj.toString().isEmpty()) {
                refund.setRespRefundFee(Integer.valueOf(respRefundFeeObj.toString()));
            }

            Object refundStatusObj = respMap.get(WeChat.REFUND_STATUS_KEY + num);
            if (null != refundStatusObj) {
                refund.setStatus(refundStatusObj.toString());
            }

            Object refundTimeObj = respMap.get(WeChat.REFUND_SUCCESS_TIME_KEY + num);
            if (null != refundTimeObj) {
                refund.setSuccessTime(refundTimeObj.toString());
            }

            Object recvAccountObj = respMap.get(WeChat.RECV_ACCOUNT_KEY + num);
            if (null != recvAccountObj) {
                refund.setRefundRecvAccount(recvAccountObj.toString());
            }

            Object refundAccountObj = respMap.get(WeChat.REFUND_ACCOUNT_KEY+num);
            if (null != refundAccountObj){
                refund.setRefundAccount(refundAccountObj.toString());
            }
        }
    }


    @Override
    public void
    queryPayment(Payment payment, String apiType) throws Exception{
        String functionDescription = "微信小程序查询订单接口 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} orderId= {} iAppId={}",functionDescription,payment.getOrderId(),payment.getiAppId());

        String appId = getAppId(apiType,payment.getiAppId());
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,appId);
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandomStringByLength(32));
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, payment.getOrderId());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",functionDescription,postBody);

        String respXml;

        respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_PAYMENT_QUERY_URL);

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

        Object tranIdObj = respMap.get(WeChat.RESP_TRANSACTION_ID_KEY);
        if (null != tranIdObj){
            payment.setTransactionId(tranIdObj.toString());
        }
        Object timeEndObj = respMap.get(WeChat.RESP_TIME_END_KEY);
        if (null != timeEndObj){
            payment.setTimeEnd(timeEndObj.toString());
        }
        Object cashFeeObj = respMap.get(WeChat.RESP_CASH_FEE_KEY);
        if (null != cashFeeObj && !cashFeeObj.toString().isEmpty()){
            payment.setCashFee(Integer.valueOf(cashFeeObj.toString()));
        }
        Object totalFeeObj = respMap.get(WeChat.TOTAL_FEE_KEY);
        if (null != totalFeeObj && !totalFeeObj.toString().isEmpty()){
            payment.setRespTotalFee(Integer.valueOf(totalFeeObj.toString()));
        }
        Object bankTypeObj = respMap.get(WeChat.BANK_TYPE_KEY);
        if (null != bankTypeObj ){
            payment.setBankType(bankTypeObj.toString());
        }

        Object tradeStateObj = respMap.get(WeChat.TRADE_STATE_KEY);
        if (null == tradeStateObj){
            log.error(MyErrorCode.WECHAT_TRADE_STATUS_BLANK);
        }else{
            String tradeState = tradeStateObj.toString();
            payment.setResult(tradeState);
            if (WeChat.RETURN_CODE_SUCCESS.equals(tradeState)){
                payment.setStatus(PaymentStatusType.OK.getCode());
            }else{
                if (WeChat.RETURN_CODE_FAIL.equals(tradeState)){
                    payment.setStatus(PaymentStatusType.FAILED.getCode());
                }
            }
        }
        Object tradeDescObj = respMap.get(WeChat.TRADE_STATE_DESC_KEY);
        if (null == tradeDescObj || tradeDescObj.toString().isEmpty()){
            log.error(MyErrorCode.WECHAT_TRADE_STATUS_BLANK);
        }else{
            payment.setComments(tradeDescObj.toString());
        }


    }

    @Override
    public boolean
    closePayment(Payment payment, String apiType) throws Exception{
        String functionDescription = "微信小程序关闭订单接口 ";//Thread.currentThread().getStackTrace()[1].getMethodName();
        log.info("{} orderId= {}",functionDescription,payment.getOrderId());

        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put(WeChat.APP_ID_KEY,getAppId(apiType,payment.getiAppId()));
        paramMap.put(WeChat.MERCHANT_ID_KEY, WeChat.MINI_APP_PAYMENT_MCH_ID);
        paramMap.put(WeChat.NONCE_STRING_KEY, XmlUtil.getRandomStringByLength(32));
        paramMap.put(WeChat.OUT_TRADE_NO_KEY, payment.getOrderId());

        String sign = signParam(paramMap);
        paramMap.put(WeChat.SIGN_KEY,sign);

        String postBody = XmlUtil.map2xml(paramMap);
        log.info("{} post body : {}",functionDescription,postBody);

        String respXml;

        respXml = HttpClient431Util.doPostXml(postBody,WeChat.MINI_APP_PAYMENT_CLOSE_URL);

        if (null == respXml || respXml.isEmpty()){
            String msg = MyErrorCode.WECHAT_API_RESP_MSG_MISSING;
            log.error("{} {}",functionDescription,msg);
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

        checkResultCode(respMap);

        return true;
    }

    @Override
    public boolean
    isValidIAppId(String iAppId){
        if (null == iAppId || iAppId.isEmpty()){
            return false;
        }

        AllConfigurationBean bean = getConfig(iAppId);
        return (null != bean && null != bean.getIAppId());
    }

    @Override
    public void
    updateWeChatConfigByVendor(String renterId, String wechatId){
        renterConfig.freshWxConfig(renterId,wechatId);
    }

    private AllConfigurationBean
    convertVendorWechatConfig(VendorWechatConfig v, String iAppId){
        AllConfigurationBean bean = new AllConfigurationBean();
        bean.setIAppId(iAppId);
        bean.setJsAPIAppId(v.getJsAppId());
        bean.setJsAPIAppSecret(v.getJsAppSecret());
        bean.setMiniAppApiUrl(v.getApiUrl());
        bean.setMiniAppId(v.getAppId());
        bean.setMiniAppSecret(v.getAppSecret());
        bean.setPayNotify(v.getPayNotifyUrl());
        bean.setRefundNotify(v.getRefundNotifyUrl());

        return bean;
    }

    private AllConfigurationBean getConfig(String iAppId) {
        String functionDescription = "获取配置项: ";
        if (null == iAppId || iAppId.isEmpty()){
            log.error("{} iAppId 缺失",functionDescription);
            return null;
        }

        VendorWechatConfig v = renterConfig.getWxConfig(null,iAppId);
        if(null != v){
            return convertVendorWechatConfig(v,iAppId);
        }

        List<AllConfigurationBean> list = configuration.getIds();
        if (null == list || 0 == list.size()){
            log.error("{} 没有发现任何配置项",functionDescription);
            return null;
        }

        for (AllConfigurationBean b: list){
            if (b.getIAppId().equals(iAppId)){
                log.info("{} {}",functionDescription,JSON.toJSONString(b));
                return b;
            }
        }

        log.error("{} 没有发现iAppId={} 的配置项",functionDescription,iAppId);
        return null;
    }

    private Map<String, String>
    buildParams(String apiType,AllConfigurationBean config){
        Map<String, String> params = new HashMap<>();
        if (ApiType.JSAPI.getCode().equals(apiType)){
            params.put(WeChat.APP_ID_KEY, config.getJsAPIAppId());
            params.put(WeChat.SECRET_KEY, config.getJsAPIAppSecret());
        }else {
            params.put(WeChat.APP_ID_KEY, config.getMiniAppId());
            params.put(WeChat.SECRET_KEY, config.getMiniAppSecret());
        }

        return params;
    }

    private void
    checkResultCode(Map<String, Object> respMap){
        Object resultCodeObj = respMap.get(WeChat.RESULT_CODE_KEY);
        if (null == resultCodeObj || resultCodeObj.toString().isEmpty()){
            throw new RuntimeException(MyErrorCode.WECHAT_API_RESULT_CODE_MISSING);
        }

        String resultCode = resultCodeObj.toString();
        if (!WeChat.RETURN_CODE_SUCCESS.equals(resultCode)){
            //业务失败
            Object errCodeObj = respMap.get(WeChat.RESP_ERR_CODE_KEY);
            Object errMsgObj = respMap.get(WeChat.RESP_ERR_MESSAGE_KEY);
            String errMsg = " ";
            if (null != errCodeObj && !errCodeObj.toString().isEmpty()){
                errMsg += errCodeObj.toString();
            }
            if (null != errMsgObj && !errMsgObj.toString().isEmpty()){
                errMsg += " "+errMsgObj.toString();
            }

            log.info("微信返回错误 {}",errMsg);
            throw new RuntimeException(MyErrorCode.WECHAT_API_FAILED+errMsg);
        }
    }
}

