package com.fengchao.miniapp.bean;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value="微信预下单Bean")
public class WechatPrepayBean {

    private String timeStamp;
    private String sign;
    private String nonceStr;
    private String prepayId;
    private String resultMsg;

}
