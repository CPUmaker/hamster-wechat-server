package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserContactQuery extends BaseParam {

    private String userId;

    private String contactorId;

    private Short contactType;

    private Short status;
    private Short[] statusIn;

    private Date lastUpdateTime;
    private Date lastUpdateTimeStart;
    private Date lastUpdateTimeEnd;

    private Date createdAt;
    private Date createdAtStart;
    private Date createdAtEnd;

    /*
     * Relation queries
     */
    private TableJoinType includesUser;
    private TableJoinType includesUserContactor;
    private TableJoinType includesGroupContactor;
}
