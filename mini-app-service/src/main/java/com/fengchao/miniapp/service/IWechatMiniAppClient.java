package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.WeChatSessionResultBean;
import com.fengchao.miniapp.bean.WeChatTokenResultBean;

public interface IWechatMiniAppClient {
    WeChatTokenResultBean getAccessToken()throws Exception;

    WeChatSessionResultBean getSession(String jsCode)throws Exception;
}
