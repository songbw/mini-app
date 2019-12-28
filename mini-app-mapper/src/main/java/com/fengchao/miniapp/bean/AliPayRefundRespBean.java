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

    @ApiModelProperty(value="向支付宝申请退款单号(由本模块生成appId + timeStamp + random)", example="11111")
    private String remoteRefundNo;

    @ApiModelProperty(value="支付宝交易号", example="11111")
    private String aliPayTradeNo;

    @ApiModelProperty(value="退款支付时间", example="2014-11-27 15:45:57")
    private Date refundDate;

    @ApiModelProperty(value="退款金额(元)", example="1.11")
    private String refundFee;

    @ApiModelProperty(value="支付宝用户登录ID", example="11111")
    private String aliPayLoginId;

    @ApiModelProperty(value="买家在支付宝的用户id", example="11111")
    private String buyerId;

    @ApiModelProperty(value="comments", example="退款申请成功")
    private String comments;

}
