package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSessionUserQuery extends BaseParam {

    private String userId;

    private String contactorId;

    private String sessionId;

    private String contactorName;
}
