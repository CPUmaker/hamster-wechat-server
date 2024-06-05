package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum FileTypeEnum {
    NORMAL_FILE((short) 1, "Normal File"),
    VIDEO((short) 2, "Video"),
    IMAGE((short) 3, "Image"),
    RELEASE_PACKAGE((short) 4, "Release Package"),
    AVATAR((short) 5, "Avatar");

    private final Short type;
    private final String description;

    FileTypeEnum(Short type, String description) {
        this.type = type;
        this.description = description;
    }

    public static FileTypeEnum getByType(Short type) {
        for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
            if (fileTypeEnum.type.equals(type)) {
                return fileTypeEnum;
            }
        }
        throw new IllegalArgumentException("Invalid FileTypeEnum");
    }
}
