package com.hamsterwhat.wechat.websocket.netty.attribute;

import io.netty.util.AttributeKey;

public class Attributes {

    private Attributes() {}

    public static final AttributeKey<String> AUTH_USER_ID = AttributeKey.valueOf("authUserId");

    public static final AttributeKey<Boolean> FORCE_OFFLINE = AttributeKey.valueOf("forceOffline");
}
