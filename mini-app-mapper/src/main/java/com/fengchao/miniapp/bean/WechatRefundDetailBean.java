package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="微信退款查询每笔详情Bean")
public class WechatRefundDetailBean {

    @ApiModelProperty(value = "商户退款单号", example = "11111111")
    private String refundNo;

    @ApiModelProperty(value = "微信退款单号", example = "11111111")
    private String wxRefundNo;

    @ApiModelProperty(value = "申请退款金额(分)", example = "11")
    private Integer refundFee;

    @ApiModelProperty(value = "微信退款状态(SUCCESS—退款成功 REFUNDCLOSE—退款关闭 PROCESSING—退款处理中 CHANGE—退款异常)", example = "SUCCESS")
    private String refundStatus;

    @ApiModelProperty(value = "退款入账账户(1,退回银行卡:{银行名称}{卡类型}{卡尾号} 2,退回支付用户零钱 3,退还商户 4,退回支付用户零钱通)", example = "招商银行信用卡0403")
    private String recvAccount;

    @ApiModelProperty(value = "申请退款完成时间", example = "2019-07-25 15:26:26")
    private String refundTime;

}
