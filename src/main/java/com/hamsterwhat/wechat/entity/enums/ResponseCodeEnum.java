package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum ResponseCodeEnum {

    // Success Codes
    CODE_200(200, "Success"),
    CODE_201(201, "Created"),
    CODE_202(202, "Accepted"),

    // Client Error Codes
    CODE_400(400, "Bad Request"),
    CODE_401(401, "Unauthorized"),
    CODE_403(403, "Forbidden"),
    CODE_404(404, "Not Found"),
    CODE_405(405, "Method Not Allowed"),
    CODE_409(409, "Conflict"),

    // Server Error Codes
    CODE_500(500, "Internal Server Error"),
    CODE_501(501, "Not Implemented"),
    CODE_502(502, "Bad Gateway"),
    CODE_503(503, "Service Unavailable"),
    CODE_504(504, "Gateway Timeout"),

    // Custom Error Codes
    CODE_600(600, "Request Parameter Error"),
    CODE_601(601, "Duplicate Key Error"),
    CODE_602(602, "File Not Exist"),
    CODE_901(901, "Login Timeout"),
    CODE_902(902, "Not Friend"),
    CODE_903(903, "No Longer In The Group");

    private final int code;
    private final String message;

    ResponseCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return this.code + " " + this.message;
    }
}