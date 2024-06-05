package com.hamsterwhat.wechat.exception;

import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException() {}

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getMessage());
        this.code = responseCodeEnum.getCode();
    }
}
