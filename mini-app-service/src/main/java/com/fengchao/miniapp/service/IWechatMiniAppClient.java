package com.fengchao.miniapp.service;

import com.fengchao.miniapp.bean.WeChatTokenResultBean;

public interface IWechatMiniAppClient {
    WeChatTokenResultBean getAccessToken()throws Exception;
}
