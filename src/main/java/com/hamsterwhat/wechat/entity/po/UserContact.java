package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserContact implements Serializable {

    @Serial
    private static final long serialVersionUID = 2743225067535624965L;

    private String userId;

    private String contactorId;

    private Short contactType;

    private Short status;

    private Date lastUpdateTime;

    private Date createdAt;

    /**
     * Relation field of foreign key "userId"
     */
    private UserInfo user;

    /**
     * Relation field of foreign key "contactorId" when contactType equals to USER
     */
    private UserInfo userContactor;

    /**
     * Relation field of foreign key "contactorId" when contactType equals to GROUP
     */
    private GroupInfo groupContactor;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
