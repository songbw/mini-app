package com.fengchao.miniapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorWechatConfig {
    private String wechatId;
    private String renterId;
    private String remark;
    private String appId;
    private String appSecret;
    private String apiUrl;
    private String jsAppId;
    private String jsAppSecret;
    private String jsApiUrl;
    private String payNotifyUrl;
    private String refundNotifyUrl;
}
