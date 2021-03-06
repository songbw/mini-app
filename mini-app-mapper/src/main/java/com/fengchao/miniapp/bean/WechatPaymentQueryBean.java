package com.fengchao.miniapp.bean;

import com.fengchao.miniapp.constant.MyErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ApiModel(value="微信订单查询Bean")
public class WechatPaymentQueryBean {
    @NotBlank(message = MyErrorCode.WECHAT_API_OPEN_ID_BLANK)
    @ApiModelProperty(value = "用户唯一标识", example = "1dsdsdssdsds")
    private String openId;

    @NotBlank(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK)
    @ApiModelProperty(value = "用户唯一标识", example = "11111111")
    private String orderId;
}
