package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="微信退款返回Bean")
public class WechatRefundRespBean {

    private String code;
    private String msg;
    private String refundNo;
    private String wechatRefundNo;
    private Integer refundFee;

}
