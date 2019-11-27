package com.fengchao.miniapp.constant;

public enum PaymentStatusType {
    PREPAY(0, "预下单完成"),
    OK(1, "支付成功"),
    FAILED(2, "支付失败"),
    USERPAYING(2, "用户支付中"),
    CLOSED(4, "交易关闭");

    private Integer code;
    private String msg;

    PaymentStatusType(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
