package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class GroupInfoQuery extends BaseParam {
    private Long id;

    private String groupId;

    private String groupName;
    private String groupNameLike;

    private String groupOwnerId;

    private String groupNotice;

    private Short status;

    private Date createdAt;
    private Date createdAtStart;
    private Date createdAtEnd;
}
