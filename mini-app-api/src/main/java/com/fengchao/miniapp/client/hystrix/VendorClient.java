package com.fengchao.miniapp.client.hystrix;

import com.fengchao.miniapp.client.http.IVendorClient;
import com.fengchao.miniapp.dto.VendorAlipayConfig;
import com.fengchao.miniapp.dto.VendorAppConfig;
import com.fengchao.miniapp.dto.VendorResponse;
import com.fengchao.miniapp.dto.VendorWechatConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Component
public class VendorClient implements IVendorClient {

    @Override
    public VendorResponse<VendorAppConfig>
    getAppConfig(@RequestHeader String renterId, @RequestParam String appId){
        log.error("调用vendors服务，获取多端配置失败 renterId={} appId={}",renterId,appId);
        return VendorResponse.fail("获取多端配置失败");
    }

    @Override
    public VendorResponse<VendorAlipayConfig>
    getAlipayConfig(@RequestHeader String renterId, @RequestParam String appId, @RequestParam String alipayId){
        log.error("调用vendors服务，获取支付宝配置失败 renterId={} alipayId={} ",renterId,alipayId);
        return VendorResponse.fail("获取支付宝配置失败");
    }

    @Override
    public VendorResponse<VendorWechatConfig>
    getWechatConfig(@RequestHeader String renterId, @RequestParam String appId,@RequestParam String wechatId){
        log.error("调用vendors服务，获取微信支付配置失败renterId={} wechatId={}",renterId,wechatId);
        return VendorResponse.fail("获取微信支付配置失败");
    }

    @Override
    public VendorResponse<List<VendorAlipayConfig>>
    getAlipayConfigList(){
        log.error("调用vendors服务，获取支付宝配置列表失败");
        return VendorResponse.fail("获取支付宝配置列表失败");
    }

    @Override
    public VendorResponse<List<VendorWechatConfig>>
    getWechatConfigList(){
        log.error("调用vendors服务，获取微信支付配置列表失败");
        return VendorResponse.fail("获取微信支付配置列表失败");
    }

    @Override
    public VendorResponse<List<VendorAppConfig>>
    getAppConfigList(){
        log.error("调用vendors服务，获取的多端配置列表失败");
        return VendorResponse.fail("获取多端配置列表失败");
    }
}
