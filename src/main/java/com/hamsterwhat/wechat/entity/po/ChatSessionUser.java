package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class ChatSessionUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 8649995957484331327L;

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
