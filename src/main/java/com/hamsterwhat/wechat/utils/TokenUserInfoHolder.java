package com.hamsterwhat.wechat.utils;

import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;

public class TokenUserInfoHolder {

    private static final ThreadLocal<TokenUserInfoDTO> tl = new ThreadLocal<>();

    public static void saveTokenUserInfo(TokenUserInfoDTO tokenUserInfo) {
        tl.set(tokenUserInfo);
    }

    public static TokenUserInfoDTO getTokenUserInfo() {
        return tl.get();
    }

    public static void removeTokenUserInfo() {
        tl.remove();
    }

    private TokenUserInfoHolder() {}
}
