package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.dto.UserContactSearchResultDTO;
import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;

import java.util.List;

public interface UserContactService {
    UserContact getUserContactByPrimaryKeys(String userId, String contactId);

    List<UserContact> findListByParam(UserContactQuery param);

    Integer findCountByParam(UserContactQuery param);

    UserContactSearchResultDTO searchContact(String userId, String contactorId);

    void addContact(String applyUserId, String receiveUserId, String contactId, Short contactType, String applyMsg);

    void addRobotContact(String userId);

    List<UserContact> getAllContacts(String userId, Short contactType);

    Integer deleteContact(String userId, String contactId, boolean isToBlock);
}
