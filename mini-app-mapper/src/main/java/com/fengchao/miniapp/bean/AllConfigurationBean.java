package com.fengchao.miniapp.bean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AllConfigurationBean {
    /*
    * iAppId: "01"
    miniAppId: "wxb28f0336ad53ceda"
    miniAppSecret: "ea5f6b5996b2cee31c4e942e7eb2b20f"
    miniAppApiUrl: "https://api.weixin.qq.com/"
    payNotify: "https://xcx-dev.weesharing.com/wechat/payment/notify/01"
    refundNotify: "https://xcx-dev.weesharing.com/wechat/refund/notify/01"
    jsAPIAppId: "wxe5b7d5b7722a1577"
    jsAPIAppSecret: "f17f8edb74126bb95538194ec79ffde7"

    * */
    private String iAppId;
    private String miniAppId;
    private String miniAppSecret;
    private String miniAppApiUrl;
    private String payNotify;
    private String refundNotify;
    private String jsAPIAppId;
    private String jsAPIAppSecret;

    private String aliPayAppId;
}
