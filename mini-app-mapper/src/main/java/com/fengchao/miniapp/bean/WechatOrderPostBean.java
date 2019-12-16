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
@ApiModel(value="微信统一下单Bean")
public class WechatOrderPostBean {
    @NotBlank(message = MyErrorCode.WECHAT_API_GOODS_BODY_BLANK)
    //@Size(min=6,message = MyErrorCode.WECHAT_API_OPEN_ID_WRONG)
    @ApiModelProperty(value="商品描述", example="汽车")
    private String body;

    @NotBlank(message = MyErrorCode.WECHAT_API_TRAN_NO_BLANK)
    @ApiModelProperty(value="商户订单号", example="12345")
    private String tradeNo;

    @NotBlank(message = MyErrorCode.WECHAT_API_OPEN_ID_BLANK)
    @ApiModelProperty(value="openId", example="12345")
    private String openId;

    @NotNull(message = MyErrorCode.WECHAT_API_TOTAL_FEE_BLANK)
    @ApiModelProperty(value="支付金额", example="123")
    private Integer totalFee;

    @NotBlank(message = MyErrorCode.I_APP_ID_BLANK)
    @ApiModelProperty(value="应用端号", example="01")
    private String iAppId;

}
