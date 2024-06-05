package com.hamsterwhat.wechat.entity.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserContactSearchResultDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -294151788298350895L;

    private String contactorId;

    private String contactType;

    private String contactorName;

    private Long avatarLastUpdate;

    private Short status;

    private Short gender;

    private String geo;
}
