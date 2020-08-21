package com.fengchao.miniapp.client.http;

import com.fengchao.miniapp.client.hystrix.AggPayClient;
import com.fengchao.miniapp.dto.VendorAlipayConfig;
import com.fengchao.miniapp.dto.VendorAppConfig;
import com.fengchao.miniapp.dto.VendorResponse;
import com.fengchao.miniapp.dto.VendorWechatConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "vendors", fallback = AggPayClient.class)
public interface IVendorClient {

    @RequestMapping(value = "/renter/api/appConfig", method = RequestMethod.GET)
    VendorResponse<VendorAppConfig>
    getAppConfig(@RequestHeader String renterId, @RequestParam String appId);


    @RequestMapping(value = "/renter/api/alipayInfo", method = RequestMethod.GET)
    VendorResponse<VendorAlipayConfig>
    getAlipayConfig(@RequestHeader String renterId, @RequestParam String appId, @RequestParam String alipayId);

    @RequestMapping(value = "/renter/api/wechatInfo", method = RequestMethod.GET)
    VendorResponse<VendorWechatConfig>
    getWechatConfig(@RequestHeader String renterId, @RequestParam String appId, @RequestParam String wechatId);

    @RequestMapping(value = "/renter/api/alipayInfo/list", method = RequestMethod.GET)
    VendorResponse<List<VendorAlipayConfig>>
    getAlipayConfigList();

    @RequestMapping(value = "/renter/api/wechatInfo/list", method = RequestMethod.GET)
    VendorResponse<List<VendorWechatConfig>>
    getWechatConfigList();

    @RequestMapping(value = "/renter/api/appConfig/list", method = RequestMethod.GET)
    VendorResponse<List<VendorAppConfig>>
    getAppConfigList();

}
