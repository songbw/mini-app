package com.fengchao.miniapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VendorResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    protected  VendorResponse(){}

    protected VendorResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static <T> VendorResponse<T>
    fail(String msg){
        if (null ==msg){
            msg = "fail";
        }
        return new VendorResponse<T>(400,msg,null);
    }
}
