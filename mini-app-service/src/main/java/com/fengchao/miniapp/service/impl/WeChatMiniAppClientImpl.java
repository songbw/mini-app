package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fengchao.miniapp.bean.WeChatSessionResultBean;
import com.fengchao.miniapp.bean.WeChatTokenResultBean;
import com.fengchao.miniapp.config.MiniAppConfiguration;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.constant.WeChat;
import com.fengchao.miniapp.constant.WeChatErrorCode;
import com.fengchao.miniapp.service.IWechatMiniAppClient;
import com.fengchao.miniapp.utils.HttpClient431Util;
import com.fengchao.miniapp.utils.RedisDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WeChatMiniAppClientImpl implements IWechatMiniAppClient {

    private MiniAppConfiguration configuration;

    @Autowired
    public WeChatMiniAppClientImpl(MiniAppConfiguration configuration){

        this.configuration = configuration;

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
}
