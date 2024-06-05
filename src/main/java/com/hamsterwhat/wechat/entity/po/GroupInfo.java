package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class GroupInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -8644001077824360488L;

    private Long id;

    private String groupId;

    private String groupName;

    private String groupOwnerId;

    private String groupNotice;

    private Short status;

    private Date createdAt;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
