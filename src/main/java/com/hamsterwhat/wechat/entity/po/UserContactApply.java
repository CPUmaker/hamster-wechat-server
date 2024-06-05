package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserContactApply implements Serializable {

    @Serial
    private static final long serialVersionUID = -7658046629262205382L;

    private Long id;

    private String applyUserId;

    private String receiveUserId;

    private String contactId;

    private Short contactType;

    private Short status;

    private String applyInfo;

    private Date lastApplyTime;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
