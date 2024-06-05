package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum MessageStatusEnum {
    SENDING((short) 0, "Sending"),
    SENT((short) 1, "Sent");

    private final Short status;

    private final String description;

    MessageStatusEnum(Short status, String description) {
        this.status = status;
        this.description = description;
    }
}
