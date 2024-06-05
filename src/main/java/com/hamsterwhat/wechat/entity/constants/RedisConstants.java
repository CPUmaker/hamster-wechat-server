package com.hamsterwhat.wechat.entity.constants;

public class RedisConstants {

    private static final String REDIS_KEY_PREFIX = "wechat:";

    public static final String SYSTEM_SETTINGS_KEY = REDIS_KEY_PREFIX + "sys-settings:";

    public static final String WS_USER_HEARTBEAT_KEY = REDIS_KEY_PREFIX + "ws:user:heartbeat:";
    public static final Long WS_USER_HEARTBEAT_EXPIRE = 6L;

    public static final String WS_TOKEN_KEY = REDIS_KEY_PREFIX + "ws:token:";
    public static final Long WS_TOKEN_EXPIRE = 60 * 60 * 24 * 2L;
    public static final String WS_TOKEN_DIGEST_KEY = REDIS_KEY_PREFIX + "ws:token-digest:";
    public static final Long WS_TOKEN_DIGEST_EXPIRE = 60 * 60 * 24 * 2L;

    public static final String USER_CONTACT_KEY = REDIS_KEY_PREFIX + "user:contact:";
    public static final Long USER_CONTACT_EXPIRE = 60 * 60 * 24 * 2L;
}
