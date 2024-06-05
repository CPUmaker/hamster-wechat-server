package com.hamsterwhat.wechat.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MessageDTO<T extends Serializable> implements Serializable {
    @Serial
    private static final long serialVersionUID = -3271028795517752567L;

    private Short messageType;

    private String content;

    private String sendUserId;

    private String sendUserUsername;

    private String contactorId;

    private String contactorName;

    private Short contactType;

    private T extendData;
}
