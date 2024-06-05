package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 7623951678691473161L;

    private Long id;

    private String sessionId;

    private Short messageType;

    private String content;

    private String sendUserId;

    private String sendUserUsername;

    private String contactorId;

    private Short contactType;

    private Long fileSize;

    private String fileName;

    private Short fileType;

    private Short status;

    private Date sendTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
