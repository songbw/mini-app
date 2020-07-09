package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="支付宝SDK签名参数返回Bean")
public class AliPaySignParamBean {

    @ApiModelProperty(value="签名", example="LXKI/pifOKneH1ercBgbHtZ3murNArFANLpFysylAonZw1xmI1xdttxqtP5VDWKCm3fK9M0bEIY7aVznbqL5a/pXQO/uXabJ9wAevpJ1vhZYG3mfUgWLqkGlOuFIJx7RyNFVAb2V19V6VrziSdGLrxYw/0kdhSV5J3Y/BSY//sc0EdoZgf2xaLTTzcC8uYGnnvSkZWddje7oXmMqp8tfIyUgSzhnBD8fW3B3IDv5OY+uw3lYJTfF/WoiyivaU7ccOwrf9PQZxbI/LTNyLijVGiSpDSPIonQNvkLjA+vRG2nPBXoLQ+EEniEonQ9QzkYD07twksdBfyVocIblZ/5mmw==")
    private String sign;

    @ApiModelProperty(value="签名类型", example="RSA2")
    private String signType;

    @ApiModelProperty(value="appId", example="11")
    private String appId;

    @ApiModelProperty(value="签名后请求串", example="app_id=2021001146662264&biz_content={\"out_trade_no\":\"11201900011234\",\"product_code\":\"QUICK_MSECURITY_PAY\",\"subject\":\"大乐透\",\"total_amount\":\"0.01\"}&charset=utf-8&method=alipay.trade.app.pay&notify_url=https://api.weesharing.com/v2/miniapp/aliPay/payment/notify&sign_type=RSA2&timestamp=2020-07-09 23:48:08&version=1.0")
    private String signedRequestString;

}
