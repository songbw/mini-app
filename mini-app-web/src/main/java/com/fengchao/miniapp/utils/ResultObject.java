package com.fengchao.miniapp.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultObject<T>{
    private Integer code;
    private String msg;
    private T data;

    public ResultObject(Integer code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

}
