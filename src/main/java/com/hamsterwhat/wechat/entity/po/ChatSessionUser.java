package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@Data
public class ChatSessionUser {

    private String userId;

    private String contactorId;

    private String sessionId;

    private String contactorName;

    /*
     * Relation query from ChatSession
     */

    private String lastMessage;

    private Date lastReceiveTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
