package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@Data
public class GroupInfo {

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
