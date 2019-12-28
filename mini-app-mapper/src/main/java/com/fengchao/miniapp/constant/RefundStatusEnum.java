package com.fengchao.miniapp.constant;
/**
 * 退款状态
 * @author Clark
 * @date 2019/12/28
 */
public enum RefundStatusEnum {
    /*退款成功*/
    SUCCESS("success", "成功"),
    /*退款失败*/
    FAILED("failed", "失败"),
    /*支付宝暂时无此情况*/
    PENDING("pending", "等待结果");

    private String code;
    private String msg;

    RefundStatusEnum(String code, String msg) {
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
