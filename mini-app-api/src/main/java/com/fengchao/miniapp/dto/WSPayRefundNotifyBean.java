package com.fengchao.miniapp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@ApiModel(value="聚合支付退款回调Bean")
public class WSPayRefundNotifyBean {

    @ApiModelProperty(value="支付退款流水号", example="CF00000001")
    private String refundNo;

    @ApiModelProperty(value="退款金额", example="1.11")
    private String refundFee;

    @ApiModelProperty(value="退款日期", example="2019-11-11 00：00：00")
    private String tradeDate;

    @ApiModelProperty(value="聚合支付中标记的支付类型, 凤巢微信小程序支付:fcwxxcx  凤巢微信公众号支付:fcwx  凤巢微信H5支付:fcwxh5", example="fcwxxcx")
    private String payType;

    @ApiModelProperty(value="订单号,此处为申请退款时的orderId", example="1100000001")
    private String orderNo;

    @ApiModelProperty(value="微信支付的退款流水号", example="wx00000001")
    private String tradeNo;


}

