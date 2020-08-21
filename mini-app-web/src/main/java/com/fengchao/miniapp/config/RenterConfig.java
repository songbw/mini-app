package com.fengchao.miniapp.config;

import com.alibaba.fastjson.JSON;
import com.fengchao.miniapp.client.http.IVendorClient;
import com.fengchao.miniapp.dto.VendorAlipayConfig;
import com.fengchao.miniapp.dto.VendorAppConfig;
import com.fengchao.miniapp.dto.VendorResponse;
import com.fengchao.miniapp.dto.VendorWechatConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Configuration
@Component
public class RenterConfig {

    @Autowired
    private IVendorClient vendorClient;

    List<VendorWechatConfig> wxConfigs;
    List<VendorAlipayConfig> aliConfigs;
    List<VendorAppConfig> appConfigs;

    @PostConstruct//在servlet初始化的时候加载，并且只加载一次，和构造代码块的作用类似
    private void init() throws Exception{
        log.info("获取服务端支付配置");

        VendorResponse<List<VendorAlipayConfig>> alipayConfigResp = vendorClient.getAlipayConfigList();
        VendorResponse<List<VendorWechatConfig>> wechatConfigResp = vendorClient.getWechatConfigList();
        VendorResponse<List<VendorAppConfig>>  appConfigResp = vendorClient.getAppConfigList();

        wxConfigs = wechatConfigResp.getData();
        aliConfigs = alipayConfigResp.getData();
        appConfigs = appConfigResp.getData();

        log.info("获取多端配置 {}", JSON.toJSONString(appConfigs));
        //log.info("获取服务端支付配置 {}", JSON.toJSONString(wxConfigs));
        //log.info("获取服务端支付配置 {}", JSON.toJSONString(aliConfigs));

    }

    public VendorAlipayConfig
    getAliPayConfig(String renterId, String appId){
        VendorAppConfig appConfig = getAppConfig(renterId,appId);
        if(null == appConfig){
            return null;
        }
        for(VendorAlipayConfig v: aliConfigs){
            if(v.getAlipayId().equals(appConfig.getAlipayId())){
                return v;
            }
        }
        return null;
    }

    public VendorWechatConfig
    getWxConfig(String renterId, String appId){
        VendorAppConfig appConfig = getAppConfig(renterId,appId);
        if(null == appConfig){
            return null;
        }
        for(VendorWechatConfig v: wxConfigs){
            if(v.getWechatId().equals(appConfig.getWechatId())){
                return v;
            }
        }
        return null;
    }

    public void
    freshAliConfig(String renterId, String alipayId){
        log.info("获取支付宝配置 参数: renterId={}, alipayId={}",renterId,alipayId);
        VendorResponse<VendorAlipayConfig> alipayConfigResp = vendorClient.getAlipayConfig(renterId,alipayId,alipayId);
        log.info("获取支付宝配置 返回: {}",JSON.toJSONString(alipayConfigResp));
        VendorAlipayConfig vc = alipayConfigResp.getData();
        if(null != vc && null != vc.getRenterId() && null != vc.getAlipayId()) {
            for(VendorAlipayConfig v: aliConfigs){
                if(v.getRenterId().equals(vc.getRenterId()) && v.getAlipayId().equals(vc.getAlipayId())){
                    BeanUtils.copyProperties(vc,v);
                    log.info("更新支付宝配置 {}",JSON.toJSONString(vc));
                    return;
                }
            }
            aliConfigs.add(vc);
            log.info("增加支付宝配置 {}",JSON.toJSONString(vc));
        }else{
            log.info("更新支付宝配置 失败");
        }
    }

    public void
    freshWxConfig(String renterId, String wechatId){
        log.info("获取微信配置 参数: renterId={}, wechatId={}",renterId,wechatId);
        VendorResponse<VendorWechatConfig> wxConfigResp = vendorClient.getWechatConfig(renterId,wechatId,wechatId);
        log.info("获取微信配置 返回: {}",JSON.toJSONString(wxConfigResp));
        VendorWechatConfig vw = wxConfigResp.getData();
        if(null != vw && null != vw.getRenterId() && null != vw.getWechatId()) {
            for(VendorWechatConfig v: wxConfigs){
                if(v.getRenterId().equals(renterId) && v.getWechatId().equals(vw.getWechatId())){
                    BeanUtils.copyProperties(vw,v);
                    log.info("更新微信支付配置 {}",JSON.toJSONString(vw));
                    return;
                }
            }
            wxConfigs.add(vw);
            log.info("增加微信支付配置 {}",JSON.toJSONString(vw));
        }else{
            log.info("更新微信支付配置 失败");
        }

    }

    public void
    freshAppConfig(String renterId, String appId){
        log.info("获取多端配置 参数: renterId={} appId={}",renterId, appId);
        VendorResponse<VendorAppConfig> appConfigResp = vendorClient.getAppConfig(renterId,appId);
        log.info("获取多端配置 返回: {}",JSON.toJSONString(appConfigResp));
        VendorAppConfig va = appConfigResp.getData();
        if(null != va && null != va.getRenterId() && null != va.getAppId()) {
            for(VendorAppConfig v: appConfigs){
                if(v.getRenterId().equals(renterId) && v.getAppId().equals(va.getAppId())){
                    BeanUtils.copyProperties(va,v);
                    log.info("更新多端配置 {}",JSON.toJSONString(va));
                    return;
                }
            }
            appConfigs.add(va);
            log.info("增加多端配置 {}",JSON.toJSONString(va));
        }else{
            log.info("更新多端配置 失败");
        }

    }

    /**==== private ==== */
    private VendorAppConfig
    getAppConfig(String renterId, String appId){
        for(VendorAppConfig v: appConfigs){
            if(v.getAppId().equals(appId) && v.getRenterId().equals(renterId)){
                return v;
            }
        }
        return null;
    }
}
