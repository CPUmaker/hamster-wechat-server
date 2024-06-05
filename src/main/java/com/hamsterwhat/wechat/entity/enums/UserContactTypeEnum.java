package com.hamsterwhat.wechat.entity.enums;

import com.hamsterwhat.wechat.utils.StringUtils;
import lombok.Getter;

@Getter
public enum UserContactTypeEnum {
    USER((short) 0, "U", "user"),
    GROUP((short) 1, "G", "group");

    private final Short type;

    private final String prefix;

    private final String description;

    UserContactTypeEnum(Short type, String prefix, String description) {
        this.type = type;
        this.prefix = prefix;
        this.description = description;
    }

    public static UserContactTypeEnum getByPrefix(String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            throw new IllegalArgumentException("Invalid prefix");
        }
        for (UserContactTypeEnum userContactTypeEnum : UserContactTypeEnum.values()) {
            if (userContactTypeEnum.getPrefix().equals(prefix)) {
                return userContactTypeEnum;
            }
        }
        throw new IllegalArgumentException("Invalid prefix");
    }

    public static UserContactTypeEnum getByType(Short type) {
        for (UserContactTypeEnum userContactTypeEnum : UserContactTypeEnum.values()) {
            if (userContactTypeEnum.getType().equals(type)) {
                return userContactTypeEnum;
            }
        }
        throw new IllegalArgumentException("Invalid user contact type: " + type);
    }
}
