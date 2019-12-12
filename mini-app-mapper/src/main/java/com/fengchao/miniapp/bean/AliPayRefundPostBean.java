package com.fengchao.miniapp.bean;

import com.fengchao.miniapp.constant.MyErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ApiModel(value="支付宝退款申请Bean")
public class AliPayRefundPostBean {

    @NotBlank(message = MyErrorCode.REFUND_NO_BLANK)
    @ApiModelProperty(value = "工单系统退款号", example = "11111")
    private String refundNo;

    @NotBlank(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK)
    @ApiModelProperty(value = "商户订单号", example = "11111111")
    private String orderId;

    @NotNull(message = MyErrorCode.WECHAT_API_REFUND_FEE_NULL)
    @ApiModelProperty(value = "退款金额(分)", example = "111")
    private Integer refundFee;

    @NotNull(message = MyErrorCode.WECHAT_API_ORDER_FEE_NULL)
    @ApiModelProperty(value = "订单金额", example = "111")
    private Integer totalFee;

    @ApiModelProperty(value = "用户唯一标识", example = "1dsdsdssdsds")
    private String openId;

}
