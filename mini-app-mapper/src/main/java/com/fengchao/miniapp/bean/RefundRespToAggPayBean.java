package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="退款请求返回给工单/聚合支付的结果Bean")
public class RefundRespToAggPayBean {

    private String code;
    private String msg;

    @ApiModelProperty(value = "由工单/聚合支付发出退款请求时传送过来的单号(由工单系统/聚合支付生成)", example = "111575631422747")
    private String refundNo;

    @ApiModelProperty(value = "由本模块生成向微信发出退款请求的单号(appId+timestamp+random)", example = "wxb28f0336ad53ceda1575627870248")
    private String remoteRefundNo;

    private Integer refundFee;

}
