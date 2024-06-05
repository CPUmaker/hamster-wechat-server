package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum UserStatusEnum {
    DISABLED((short) 0, "Disabled"),
    ENABLED((short) 1, "Enabled");

    private final Short status;
    private final String description;

    UserStatusEnum(Short status, String description) {
        this.status = status;
        this.description = description;
    }

    public static UserStatusEnum getByStatus(Short status) {
        for (UserStatusEnum userStatusEnum : UserStatusEnum.values()) {
            if (userStatusEnum.getStatus().equals(status)) {
                return userStatusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown status in UserStatusEnum: " + status);
    }
}
