package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ChatSessionQuery extends BaseParam {

    private String id;

    private String lastMessage;

    private Date lastReceiveTime;
}
