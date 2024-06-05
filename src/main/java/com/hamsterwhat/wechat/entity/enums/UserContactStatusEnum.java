package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum UserContactStatusEnum {
    NOT_CONNECTED((short) 0, "Not Connected"),
    FRIEND((short) 1, "Friend"),
    DELETED((short) 2, "Deleted By Self"),
    DELETED_BY_OTHER((short) 3, "Deleted By Other"),
    BLOCKED((short) 4, "Blocked By Self"),
    BLOCKED_BY_OTHER((short) 5, "Blocked By Other"),
    PENDING((short) 0, "Pending Request");

    private final Short status;
    private final String description;

    UserContactStatusEnum(Short status, String description) {
        this.status = status;
        this.description = description;
    }

    public static UserContactStatusEnum getByStatus(Short status) {
        for (UserContactStatusEnum userContactStatusEnum : UserContactStatusEnum.values()) {
            if (userContactStatusEnum.getStatus().equals(status)) {
                return userContactStatusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown status in UserContactStatusEnum: " + status);
    }
}
