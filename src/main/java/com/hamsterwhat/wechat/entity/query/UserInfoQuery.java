package com.hamsterwhat.wechat.entity.query;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class UserInfoQuery extends BaseParam {
    private Long id;

    private String userId;
    private String userIdLike;

    private String email;
    private String emailLike;

    private String username;
    private String usernameLike;

    private Short gender;

    private String password;

    private String bio;

    private Short status;

    private String geo;

    private Boolean isAdmin;

    private Date lastLoginTime;
    private Date lastLoginTimeStart;
    private Date lastLoginTimeEnd;

    private Date lastOfflineTime;
    private Date lastOfflineTimeStart;
    private Date lastOfflineTimeEnd;

    private Date createdAt;
    private Date createdAtStart;
    private Date createdAteEnd;
}
