package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="支付宝SDK签名参数返回Bean")
public class AliPaySignParamBean {

    @ApiModelProperty(value="签名", example="sfdkfjdskfj111dsdsd1112")
    private String sign;

    @ApiModelProperty(value="签名类型", example="RSA2")
    private String signType;

    @ApiModelProperty(value="appId", example="212121")
    private String appId;

}
