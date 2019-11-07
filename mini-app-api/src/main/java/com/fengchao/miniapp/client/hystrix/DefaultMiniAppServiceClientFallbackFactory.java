package com.fengchao.miniapp.client.hystrix;

import com.fengchao.miniapp.client.http.MiniAppServiceClient;
import com.fengchao.miniapp.dto.RPCResponse;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultMiniAppServiceClientFallbackFactory implements FallbackFactory<MiniAppServiceClient> {

    @Override
    public MiniAppServiceClient create(Throwable throwable) {
        return new MiniAppServiceClient() {

            @Override
            public RPCResponse getAccessToken(String jsCode) {
                return HystrixDefaultFallback.defaultFallback(throwable);
            }
        };
    }

}

