package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="微信小程序获取Session结果Bean")
public class WeChatSessionResultBean {//auth.code2Session

    @ApiModelProperty(value="用户唯一标识", example="1dsdsdssdsds")
    private String openid;

    @ApiModelProperty(value="会话密钥", example="1dsdsdssdsds")
    private String session_key;

    @ApiModelProperty(value="用户在开放平台的唯一标识符", example="1dsdsdssdsds")
    private String unionid;

    @ApiModelProperty(value="0:新用户,其他为老用户 ", example="1")
    private String isNewUser;


}
