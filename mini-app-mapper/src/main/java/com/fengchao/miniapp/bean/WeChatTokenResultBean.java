package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="微信小程序获取Token结果Bean")
public class WeChatTokenResultBean {//与微信小程序服务接口:auth.getAccessToken 保持一致
    @ApiModelProperty(value="获取到的凭证", example="1dsdsdssdsds")
    private String access_token;

    @ApiModelProperty(value="凭证有效时间，单位：秒。目前是7200秒之内的值", example="7200")
    private Integer expires_in;

}
