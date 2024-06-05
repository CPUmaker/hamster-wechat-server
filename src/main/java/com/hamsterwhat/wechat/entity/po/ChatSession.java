package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class ChatSession implements Serializable {

    @Serial
    private static final long serialVersionUID = -5724255321404073679L;

    private String id;

    private String lastMessage;

    private Date lastReceiveTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
