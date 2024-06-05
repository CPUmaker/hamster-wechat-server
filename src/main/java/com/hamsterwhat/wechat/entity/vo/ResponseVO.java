package com.hamsterwhat.wechat.entity.vo;

import lombok.Data;

@Data
public class ResponseVO<T> {

    private String status;

    private Integer code;

    private String msg;

    private T data;
}
