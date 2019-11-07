package com.fengchao.miniapp.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fengchao.miniapp.bean.WeChatTokenResultBean;
import com.fengchao.miniapp.config.MiniAppConfiguration;
import com.fengchao.miniapp.constant.MyErrorCode;
import com.fengchao.miniapp.constant.WeChat;
import com.fengchao.miniapp.service.IWechatMiniAppClient;
import com.fengchao.miniapp.utils.HttpClient431Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public WeChatTokenResultBean
    getAccessToken()
            throws Exception{
        String _func = Thread.currentThread().getStackTrace()[1].getMethodName();

        String url = configuration.getWechatAppApiUrl() +WeChat.GET_ACCESS_TOKEN_PATH;
        Map<String,String> params = new HashMap<>();
        params.put(WeChat.GRANT_TYPE_KEY,WeChat.GRANT_TYPE_DEFAULT);
        params.put(WeChat.APP_ID_KEY,configuration.getWechatAppId());
        params.put(WeChat.SECRET_KEY,configuration.getWechatAppSecret());
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

        String token = json.getString(WeChat.ACCESS_TOKEN_KEY);
        Integer expires = json.getInteger(WeChat.EXPIRES_IN_KEY);
        if (null == token || token.isEmpty()){
            //Integer errCode = json.getInteger(WeChat.ERR_CODE_KEY);
            String errMsg = MyErrorCode.WECHAT_API_FAILED+json.getString(WeChat.ERR_MESSAGE_KEY);
            log.error(_func+errMsg);
            throw new Exception(errMsg);
        }

        WeChatTokenResultBean bean = new WeChatTokenResultBean();
        bean.setAccess_token(token);
        bean.setExpires_in(expires);
        log.info(_func+WeChat.GET_ACCESS_TOKEN_API+JSON.toJSONString(bean));
        return bean;
    }
}
