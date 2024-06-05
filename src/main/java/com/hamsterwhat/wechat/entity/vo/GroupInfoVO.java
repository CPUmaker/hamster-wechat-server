package com.hamsterwhat.wechat.entity.vo;

import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.po.UserContact;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class GroupInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -2857020564245586237L;

    private GroupInfo groupInfo;

    private Integer memberCount;

    private List<UserContact> userContacts;
}
