package com.hamsterwhat.wechat.entity.dto;

import com.hamsterwhat.wechat.entity.po.ChatMessage;
import com.hamsterwhat.wechat.entity.po.ChatSessionUser;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class WsInitDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5338212146824915294L;

    private List<ChatSessionUser> chatSessionUserList;

    private List<ChatMessage> chatMessageList;

    private Integer newApplyCount;
}
