package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(value="微信退款查询结果Bean")
public class WechatRefundListBean {

    @ApiModelProperty(value = "微信订单号", example = "11111111")
    private String transactionId;

    @ApiModelProperty(value = "订单号", example = "11111111")
    private String orderId;

    @ApiModelProperty(value = "订单金额", example = "111")
    private Integer totalFee;

    @ApiModelProperty(value = "现金支付金额", example = "111")
    private Integer cashFee;

    @ApiModelProperty(value = "退款笔数", example = "1")
    private Integer refundCount;

    private List<WechatRefundDetailBean> list;

}
