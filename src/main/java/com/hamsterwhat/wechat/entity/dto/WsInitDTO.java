package com.hamsterwhat.wechat.entity.dto;

import com.hamsterwhat.wechat.entity.po.ChatMessage;
import com.hamsterwhat.wechat.entity.po.ChatSessionUser;
import lombok.Data;

import java.util.List;

@Data
public class WsInitDTO {

    private List<ChatSessionUser> chatSessionUserList;

    private List<ChatMessage> chatMessageList;

    private Integer newApplyCount;
}
