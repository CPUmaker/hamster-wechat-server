package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum AppVersionStatusEnum {
    NOT_RELEASED((short) 1, "Not Released"),
    GRAYSCALE_RELEASE((short) 2, "Grayscale Release"),
    RELEASE((short) 3, "Release");

    private final Short type;
    private final String description;

    AppVersionStatusEnum(short type, String description) {
        this.type = type;
        this.description = description;
    }
}
