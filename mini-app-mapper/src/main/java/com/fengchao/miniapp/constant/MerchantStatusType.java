package com.fengchao.miniapp.constant;

public enum MerchantStatusType {
    OK("0", "正常"),
    FREEZE("1", "冻结"),
    CANCEL("2", "注销"),
    PENDING("3", "待审核"),
    FAILED("4", "审核失败"),
    UNSIGNED("5", "未签约");

    private String code;
    private String msg;

    MerchantStatusType(String code, String msg) {
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

}
