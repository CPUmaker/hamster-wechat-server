package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum GroupStatusEnum {
    ACTIVE((short) 1, "Active - Users can send messages"),
    INACTIVE((short) 0, "Inactive - Users cannot send messages");

    private final Short status;
    private final String description;

    GroupStatusEnum(Short status, String description) {
        this.status = status;
        this.description = description;
    }

    public static GroupStatusEnum getByStatus(Short status) {
        for (GroupStatusEnum groupStatusEnum : GroupStatusEnum.values()) {
            if (groupStatusEnum.getStatus().equals(status)) {
                return groupStatusEnum;
            }
        }
        throw new IllegalArgumentException("Unknown status in GroupStatusEnum " + status);
    }
}
