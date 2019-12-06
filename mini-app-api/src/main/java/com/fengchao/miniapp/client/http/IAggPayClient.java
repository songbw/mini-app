package com.fengchao.miniapp.client.http;

import com.fengchao.miniapp.client.hystrix.AggPayClient;
import com.fengchao.miniapp.dto.ResultObject;
import com.fengchao.miniapp.dto.WSPayPaymentNotifyBean;
import com.fengchao.miniapp.dto.WSPayRefundNotifyBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@FeignClient(value = "aggpay", fallback = AggPayClient.class)
public interface IAggPayClient {

    @RequestMapping(value = "/wspay_notify/bj/weesharing_refund", method = RequestMethod.POST)
    ResultObject<String> postAggPayRefundNotify(@RequestBody WSPayRefundNotifyBean body);

    @RequestMapping(value = "/wspay_notify/bj/weesharing_pay", method = RequestMethod.POST)
    ResultObject<String> postAggPayPaymentNotify(@RequestBody WSPayPaymentNotifyBean body);

}

