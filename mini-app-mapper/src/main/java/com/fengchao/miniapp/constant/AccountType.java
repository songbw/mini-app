package com.fengchao.miniapp.constant;

public enum AccountType {
    CREDIT("1", "信用卡"),
    REDIT("2", "借记卡");

    private String code;
    private String msg;

    AccountType(String code, String msg) {
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

    boolean isValidCode(String code){
        if ("1".equals(code)||"2".equals(code)){
            return true;
        }else{
            return false;
        }
    }
}
