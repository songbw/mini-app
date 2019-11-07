package com.fengchao.miniapp.bean;

import com.fengchao.miniapp.constant.MyErrorCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ApiModel(value="新建LoginInfo信息Bean")
public class LoginInfoPostBean {

    @NotBlank(message = MyErrorCode.WECHAT_API_OPEN_ID_BLANK)
    @Size(min=6,message = MyErrorCode.WECHAT_API_OPEN_ID_WRONG)
    @ApiModelProperty(value="用户代码", example="sfdkfjdskfj111")
    private String openId;

    @ApiModelProperty(value="品牌", example="OPPO")
    private String brand;

    @ApiModelProperty(value="型号", example="x")
    private String model;

    @ApiModelProperty(value="版本", example="11.0")
    private String version;

    @ApiModelProperty(value="系统", example="Android")
    private String system;

    @ApiModelProperty(value="平台", example="Android")
    private String platform;
}
