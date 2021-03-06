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
@ApiModel(value="微信退款申请Bean")
public class WechatRefundPostBean {

    @NotBlank(message = MyErrorCode.WECHAT_API_OPEN_ID_BLANK)
    @ApiModelProperty(value = "用户唯一标识", example = "1dsdsdssdsds")
    private String openId;

    @NotBlank(message = MyErrorCode.REFUND_NO_BLANK)
    @ApiModelProperty(value = "工单系统/聚合支付 请求退款单号", example = "11111")
    private String refundNo;

    @NotBlank(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK)
    @ApiModelProperty(value = "订单号", example = "11111111")
    private String orderId;

    @NotNull(message = MyErrorCode.WECHAT_API_ORDER_FEE_NULL)
    @ApiModelProperty(value = "订单金额", example = "111")
    private Integer totalFee;

    @NotNull(message = MyErrorCode.WECHAT_API_REFUND_FEE_NULL)
    @ApiModelProperty(value = "退款金额", example = "111")
    private Integer refundFee;

}
