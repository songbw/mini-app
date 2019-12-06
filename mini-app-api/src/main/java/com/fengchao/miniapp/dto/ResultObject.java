package com.fengchao.miniapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResultObject<T> {

    private Integer code;

    private String message;

    private T data;
}
