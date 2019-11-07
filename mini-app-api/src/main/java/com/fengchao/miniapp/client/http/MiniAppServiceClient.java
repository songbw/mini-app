package com.fengchao.miniapp.client.http;

import com.fengchao.miniapp.client.hystrix.DefaultMiniAppServiceClientFallbackFactory;
import com.fengchao.miniapp.dto.RPCResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "miniapp", url = "${rpc.feign.client.miniapp.url:}",
        fallbackFactory = DefaultMiniAppServiceClientFallbackFactory.class)
public interface MiniAppServiceClient {

    @RequestMapping(value = "/rpc/miniapp/accessToken", method = RequestMethod.POST)
    RPCResponse getAccessToken(@RequestBody String jsCode);

}
