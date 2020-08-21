package com.fengchao.miniapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorAlipayConfig {
    private String alipayId;
    private String renterId;
    private String remark;
    private String appId;
    private String privateKey;
    private String publicKey;
    private String apiUrl;
    private String payNotifyUrl;
    private String refundNotifyUrl;
}
