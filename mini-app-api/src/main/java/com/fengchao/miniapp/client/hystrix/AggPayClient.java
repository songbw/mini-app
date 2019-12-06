package com.fengchao.miniapp.client.hystrix;

import com.fengchao.miniapp.client.http.IAggPayClient;
import com.fengchao.miniapp.dto.ResultObject;
import com.fengchao.miniapp.dto.WSPayPaymentNotifyBean;
import com.fengchao.miniapp.dto.WSPayRefundNotifyBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;


@Component
public class AggPayClient implements IAggPayClient {
    @Override
    public ResultObject<String> postAggPayRefundNotify(@RequestBody WSPayRefundNotifyBean body){
        return null;
    }

    @Override
    public ResultObject<String> postAggPayPaymentNotify(@RequestBody WSPayPaymentNotifyBean body){
        return null;
    }
}
