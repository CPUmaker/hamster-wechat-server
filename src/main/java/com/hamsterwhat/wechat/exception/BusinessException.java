package com.hamsterwhat.wechat.exception;

import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import lombok.Getter;

import java.io.Serial;

@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6869684797998566355L;

    private final Integer code;

    public BusinessException() {
        this.code = 600;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 600;
    }

    public BusinessException(final Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCodeEnum responseCodeEnum) {
        super(responseCodeEnum.getMessage());
        this.code = responseCodeEnum.getCode();
    }
}
