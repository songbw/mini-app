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
@ConfigurationProperties(/*ignoreUnknownFields = true,*/ prefix = "weixin")
@Component
@Slf4j
@Getter
@Setter
public class MiniAppConfiguration {

    private String miniAppId;

    private String miniAppSecret;

    private String miniAppApiUrl;

    private String jsAPIAppId;

    private String jsAPIAppSecret;

    @PostConstruct//在servlet初始化的时候加载，并且只加载一次，和构造代码块的作用类似
    private void init() throws Exception{
        log.info("load miniApp properties start!");

        if (null == miniAppId){
            String msg = "miniAppId not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("miniAppId = {}", miniAppId);
        }
        if (null == miniAppSecret){
            String msg = "miniAppSecret not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("miniAppSecret = {}", miniAppSecret);
        }
        if (null == miniAppApiUrl){
            String msg = "miniAppApiUrl not found";
            log.error(msg);
            throw new Exception(msg);
        }else {
            log.info("miniAppApiUrl = {}", miniAppApiUrl);
        }
        log.info("jsAPIAppId = {}",jsAPIAppId);
        log.info("jsAPIAppSecret = {}",jsAPIAppSecret);
    }






}
