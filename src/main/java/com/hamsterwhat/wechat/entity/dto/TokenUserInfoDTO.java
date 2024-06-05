package com.hamsterwhat.wechat.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class TokenUserInfoDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2704277578308475242L;

    private String token;

    private String userId;

    private String username;

    private Boolean isAdmin;
}
