package com.hamsterwhat.wechat.entity.po;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = -7677689544832570750L;

    private Long id;

    private String userId;

    private String email;

    private String username;

    private Short gender;

    private String password;

    private String bio;

    private Short status;

    private String geo;

    private Boolean isAdmin;

    private Date lastLoginTime;

    private Date lastOfflineTime;

    private Date createdAt;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
