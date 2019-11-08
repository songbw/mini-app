package com.fengchao.miniapp.constant;

public enum WeChatErrorCode {
    BUSY(-1, "系统繁忙，此时请开发者稍候再试"),
    SUCCESS(0, "请求成功"),
    INVALIDCODE(40029, "code 无效"),
    LIMITED100(45011, "频率限制，每个用户每分钟100次");

    private Integer code;
    private String msg;

    WeChatErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static Integer msg2code(String status) {
        if (null != status && !status.isEmpty()) {
            int i;
            for (i = 0; i < WeChatErrorCode.values().length; i++) {
                if (WeChatErrorCode.values()[i].toString().equals(status)) {
                    return WeChatErrorCode.values()[i].getCode();
                }
            }
        }
        return -2;
    }

    public static String code2msg(Integer code) {
        if (null != code && 0 != code) {
            int i;
            for (i = 0; i < WeChatErrorCode.values().length; i++) {
                if (WeChatErrorCode.values()[i].getCode().equals(code)) {
                    return WeChatErrorCode.values()[i].getMsg();
                }
            }
        }
        return "";
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
