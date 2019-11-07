package com.fengchao.miniapp.constant;

public enum GenderType {
    UNKNOWN(0, "未知"),
    MALE(1, "男"),
    FEMALE(2, "女");

    private Integer code;
    private String msg;

    GenderType(Integer code, String msg) {
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

    boolean isValidCode(Integer code){
        if (0==code || 1==code || 2==code){
            return true;
        }else{
            return false;
        }
    }
}
