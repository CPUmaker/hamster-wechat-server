package com.hamsterwhat.wechat.entity.enums;

import lombok.Getter;

@Getter
public enum CommandTypeEnum {
    INIT((short) 0, "Init data for establishing ws connection"),
    ADD_FRIEND((short) 1, "Message for add-friend requests"),
    CHAT_MESSAGE((short) 2, "Chat message"),
    GROUP_CREATE((short) 3, "Group create"),
    CONTACT_APPLY((short) 4, "Contact apply"),
    MEDIA_CHAT((short) 5, "Media chat"),
    FILE_UPLOAD((short) 6, "File upload"),
    FORCE_OFFLINE((short) 7, "Force offline"),
    GROUP_DEACTIVATE((short) 8, "Group deactivate"),
    JOIN_GROUP((short) 9, "Join group"),
    CONTACT_INFO_UPDATE((short) 10, "Contact info update"),
    LEAVE_GROUP((short) 11, "Leave group"),
    GROUP_REMOVE_MEMBER((short) 12, "Group remove member"),;

    private final Short type;

    private final String description;

    CommandTypeEnum(Short type, String description) {
        this.type = type;
        this.description = description;
    }

    public static CommandTypeEnum getByType(Short type) {
        for (CommandTypeEnum commandTypeEnum : CommandTypeEnum.values()) {
            if (commandTypeEnum.getType().equals(type)) {
                return commandTypeEnum;
            }
        }
        throw new IllegalArgumentException("Unknown command type: " + type);
    }
}
