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
@ApiModel(value="新建UserInfo信息Bean")
public class UserInfoPostBean {

    @NotBlank(message = MyErrorCode.WECHAT_API_OPEN_ID_BLANK)
    //@Size(min=6,message = MyErrorCode.WECHAT_API_OPEN_ID_WRONG)
    @ApiModelProperty(value="用户代码", example="sfdkfjdskfj111")
    private String openId;

    @ApiModelProperty(value="电话号码", example="12345678901")
    private String mobile;

    @ApiModelProperty(value="用户昵称", example="测试用户")
    private String nickName;

    @ApiModelProperty(value="用户头像图片的 URL", example="http://example.png")
    private String avatarUrl;

    @ApiModelProperty(value="用户性别,0:未知,1：男, 2：女", example="1")
    private Integer gender;

    @ApiModelProperty(value="用户所在国家", example="China")
    private String country;

    @ApiModelProperty(value="用户所在省份", example="Beijing")
    private String province;

    @ApiModelProperty(value="用户所在城市", example="Beijing")
    private String city;

    @ApiModelProperty(value="显示 country，province，city 所用的语言(en,zh_CN,zh_TW)", example="zh_CN")
    private String language;
}
