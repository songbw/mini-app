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
@ConfigurationProperties(/*ignoreUnknownFields = true,*/ prefix = "miniApp")
@Component
@Slf4j
@Getter
@Setter
public class MiniAppConfiguration {

    /**
     *
     */
    private String WechatAppId;

    private String WechatAppSecret;

    private String WechatAppApiUrl;


    @PostConstruct//在servlet初始化的时候加载，并且只加载一次，和构造代码块的作用类似
    private void init() throws Exception{
        log.info("load miniApp properties start!");

        if (null == WechatAppId){
            String msg = "WechatAppId not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("WechatAppId = {}", WechatAppId);
        }
        if (null == WechatAppSecret){
            String msg = "WechatAppSecret not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("WechatAppSecret = {}", WechatAppSecret);
        }
        if (null == WechatAppApiUrl){
            String msg = "WechatAppApiUrl not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("WechatAppApiUrl = {}", WechatAppApiUrl);
        }

    }






}
