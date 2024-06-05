package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ChatMessageQuery extends BaseParam {

    private Long id;

    private String sessionId;
    private List<String> sessionIdIn;

    private Short messageType;

    private String content;

    private String sendUserId;

    private String sendUserUsername;

    private String contactorId;
    private List<String> contactorIdIn;

    private Short contactType;

    private Long fileSize;

    private String fileName;

    private Short fileType;

    private Short status;

    private Date sendTime;
    private Date sendTimeStart;
    private Date sendTimeEnd;
}
