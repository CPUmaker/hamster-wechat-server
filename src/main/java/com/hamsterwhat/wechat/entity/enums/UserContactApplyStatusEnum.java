package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum UserContactApplyStatusEnum {
    PENDING((short) 0, "Waiting for processing"),
    APPROVED((short) 1, "Approved"),
    REJECTED((short) 2, "Rejected"),
    BLOCKED((short) 3, "Blocked");

    private final Short status;
    private final String description;

    UserContactApplyStatusEnum(Short status, String description) {
        this.status = status;
        this.description = description;
    }

    public static UserContactApplyStatusEnum getByStatus(Short status) {
        for (UserContactApplyStatusEnum userContactApplyStatusEnum : UserContactApplyStatusEnum.values()) {
            if (userContactApplyStatusEnum.getStatus().equals(status)) {
                return userContactApplyStatusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown status in UserContactApplyStatusEnum " + status);
    }
}
