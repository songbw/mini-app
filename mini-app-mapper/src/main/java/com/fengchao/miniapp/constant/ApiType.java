package com.fengchao.miniapp.constant;

public enum ApiType {
    MINI("mini", "微信小程序支付"),
    APP("app", "微信APP支付"),
    JSAPI("jsapi", "微信JSAPI支付"),
    ALIPAY_PHONE_WEB("AliPayPhoneWeb", "支付宝手机支付");

    private String code;
    private String msg;

    ApiType(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static boolean isValidCode(String code){
        if ("mini".equals(code)||"jsapi".equals(code)||"app".equals(code)){
            return true;
        }else{
            return false;
        }
    }
}

