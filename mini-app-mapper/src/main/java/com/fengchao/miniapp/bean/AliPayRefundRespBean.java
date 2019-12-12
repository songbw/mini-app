package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;


@Getter
@Setter
@ApiModel(value="支付宝退款返回Bean")
public class AliPayRefundRespBean {

    @ApiModelProperty(value="商户订单号", example="112")
    private String tradeNo;

    @ApiModelProperty(value="商户退款号", example="11111")
    private String refundNo;

    @ApiModelProperty(value="支付宝交易号", example="11111")
    private String aliPayTradeNo;

    @ApiModelProperty(value="退款支付时间", example="2014-11-27 15:45:57")
    private Date refundDate;

    @ApiModelProperty(value="退款金额(元)", example="1.11")
    private String refundFee;

    @ApiModelProperty(value="支付宝用户登录ID", example="11111")
    private String aliPayLoginId;

}
