package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum DateTimePatternEnum {
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"), YYYY_MM_DD("yyyy-MM-dd");

    private final String pattern;

    DateTimePatternEnum(String pattern) {
        this.pattern = pattern;
    }
}
