package com.fengchao.miniapp.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="聚合支付付款回调Bean")
public class WSPayPaymentNotifyBean {

    @ApiModelProperty(value="商户订单号", example="1100000001")
    private String orderNo;

    @ApiModelProperty(value="付款金额", example="1.11")
    private String payFee;

    @ApiModelProperty(value="付款日期", example="2019-11-11 00：00：00")
    private String tradeDate;

    @ApiModelProperty(value="聚合支付中标记的支付类型, 凤巢微信小程序支付:fcwxxcx  凤巢微信公众号支付:fcwx  凤巢微信H5支付:fcwxh5", example="fcwxxcx")
    private String payType;

    @ApiModelProperty(value="微信小程序的支付号", example="4200000463201911305163844516")
    private String tradeNo;
}
