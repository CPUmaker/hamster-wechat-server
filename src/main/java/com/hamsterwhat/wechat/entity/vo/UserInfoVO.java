package com.hamsterwhat.wechat.entity.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class UserInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -287821963927348L;

    private String userId;

    private String email;

    private String username;

    private Short gender;

    private String bio;

    private Short status;

    private String geo;

    private Boolean isAdmin;

    private Date lastLoginTime;

    private Date lastOfflineTime;

    private Date createdAt;

    private String token;
}
