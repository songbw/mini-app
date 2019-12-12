package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="支付宝退款查询结果Bean")
public class AliPayRefundQueryResultBean {

    @ApiModelProperty(value = "工单系统退款单号", example = "11111111")
    private String refundNo;

    @ApiModelProperty(value = "订单号", example = "11111111")
    private String orderId;

    @ApiModelProperty(value = "订单金额(分)", example = "111")
    private String totalFee;

    @ApiModelProperty(value = "本次退款金额(分)", example = "111")
    private String refundFee;
}
