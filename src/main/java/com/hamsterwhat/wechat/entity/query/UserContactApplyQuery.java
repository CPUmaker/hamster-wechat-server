package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserContactApplyQuery extends BaseParam {

    private Long id;

    private String applyUserId;

    private String receiveUserId;

    private String contactId;

    private Short contactType;

    private Short status;

    private String applyInfo;

    private Date lastApplyTime;
    private Date lastApplyTimeStart;
    private Date lastApplyTimeEnd;

    /*
     * Relation queries
     */
    private TableJoinType includesApplyUser;
}
