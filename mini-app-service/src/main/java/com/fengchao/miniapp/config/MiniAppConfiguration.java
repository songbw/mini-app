package com.fengchao.miniapp.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 中投科信所需的配置信息从bootstrap.yml,bootstrap-*.yml中获取
 */
@ConfigurationProperties(/*ignoreUnknownFields = true,*/ prefix = "miniapp")
@Component
@Slf4j
@Getter
@Setter
public class MiniAppConfiguration {

    private String wechatAppId;

    private String wechatAppSecret;

    private String wechatAppApiUrl;


    @PostConstruct//在servlet初始化的时候加载，并且只加载一次，和构造代码块的作用类似
    private void init() throws Exception{
        log.info("load miniApp properties start!");

        if (null == wechatAppId){
            String msg = "wechatAppId not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("wechatAppId = {}", wechatAppId);
        }
        if (null == wechatAppSecret){
            String msg = "wechatAppSecret not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("wechatAppSecret = {}", wechatAppSecret);
        }
        if (null == wechatAppApiUrl){
            String msg = "wechatAppApiUrl not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("wechatAppApiUrl = {}", wechatAppApiUrl);
        }

    }






}
