package com.fengchao.miniapp.client.hystrix;

import com.fengchao.miniapp.dto.RPCResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HystrixDefaultFallback {

    public static RPCResponse defaultFallback(Throwable cause) {
        log.error("降级异常:{}", cause.getMessage(), cause);

        RPCResponse result = new RPCResponse();
        result.setCode(500);
        result.setMsg("rpc 查询失败降级");

        return result;
    }


}
