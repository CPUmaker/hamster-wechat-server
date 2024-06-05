package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum AppVersionFileTypeEnum {
    LOCAL((short) 0, "File stores at local machine"),
    BACKLINK((short) 1, "File can be downloaded at backlink url");

    private final Short type;

    private final String description;

    AppVersionFileTypeEnum(Short type, String description) {
        this.type = type;
        this.description = description;
    }

    public static AppVersionFileTypeEnum getByType(Short type) {
        for (AppVersionFileTypeEnum appVersionFileTypeEnum : AppVersionFileTypeEnum.values()) {
            if (appVersionFileTypeEnum.getType().equals(type)) {
                return appVersionFileTypeEnum;
            }
        }
        throw new IllegalArgumentException("Unknown AppVersionFileTypeEnum type: " + type);
    }
}
