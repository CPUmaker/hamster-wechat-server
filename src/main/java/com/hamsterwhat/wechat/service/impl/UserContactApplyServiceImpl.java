package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.MessageDTO;
import com.hamsterwhat.wechat.entity.enums.*;
import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.po.UserContactApply;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.entity.query.UserContactApplyQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.GroupInfoMapper;
import com.hamsterwhat.wechat.mapper.UserContactApplyMapper;
import com.hamsterwhat.wechat.mapper.UserContactMapper;
import com.hamsterwhat.wechat.mapper.UserInfoMapper;
import com.hamsterwhat.wechat.service.UserContactApplyService;
import com.hamsterwhat.wechat.service.UserContactService;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.websocket.netty.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Service
public class UserContactApplyServiceImpl implements UserContactApplyService {

    private final UserContactApplyMapper userContactApplyMapper;

    private final UserContactMapper userContactMapper;

    private final UserInfoMapper userInfoMapper;

    private final GroupInfoMapper groupInfoMapper;

    private final UserContactService userContactService;

    private final MessageHandler messageHandler;

    @Autowired
    public UserContactApplyServiceImpl(
            UserContactApplyMapper userContactApplyMapper,
            UserContactMapper userContactMapper,
            UserInfoMapper userInfoMapper,
            GroupInfoMapper groupInfoMapper,
            UserContactService userContactService,
            MessageHandler messageHandler
    ) {
        this.userContactApplyMapper = userContactApplyMapper;
        this.userContactMapper = userContactMapper;
        this.userInfoMapper = userInfoMapper;
        this.groupInfoMapper = groupInfoMapper;
        this.userContactService = userContactService;
        this.messageHandler = messageHandler;
    }

    @Override
    public PaginationResultVO<UserContactApply> findPageByParam(UserContactApplyQuery param) {
        Integer pageSize = param.getPageSize();
        Integer currentPage = param.getPage();

        List<UserContactApply> applyList = this.userContactApplyMapper.selectUserContactApplyListByQuery(param);

        param.setPage(null);
        param.setPageSize(null);
        Integer totalCount = this.userContactApplyMapper.selectUserContactApplyCountByQuery(param);

        PaginationResultVO<UserContactApply> result = new PaginationResultVO<>();
        result.setTotalCount(totalCount);
        result.setTotalPage(totalCount / pageSize);
        result.setCurrentPage(currentPage);
        result.setPageSize(pageSize);
        result.setList(applyList);
        return result;
    }

    @Override
    public void createRequest(String userId, String username, String contactorId, String applyMsg) {
        UserContactTypeEnum contactType = UserContactTypeEnum.getByPrefix(contactorId.substring(0, 1));

        // Check if the account which the user apply to be blocked the user or not
        UserContact userContact = this.userContactMapper.selectUserContactByPrimaryKeys(contactorId, userId);
        if (userContact != null && UserContactStatusEnum.BLOCKED.getStatus().equals(userContact.getStatus())) {
            throw new BusinessException("You have been blocked by this user!");
        }

        if (StringUtils.isEmpty(applyMsg)) {
            applyMsg = String.format(SystemConstants.DEFAULT_APPLY_MSG, username);
        }
        String applyReceiveUserId;

        // Get and check contactor info accordingly
        if (contactType == UserContactTypeEnum.USER) {
            UserInfo userInfo = this.userInfoMapper.selectUserInfoByUserId(contactorId);
            if (userInfo == null) {
                throw new BusinessException("This user does not exist!");
            }
            applyReceiveUserId = userInfo.getUserId();
        } else if (contactType == UserContactTypeEnum.GROUP) {
            GroupInfo groupInfo = this.groupInfoMapper.selectGroupInfoByGroupId(contactorId);
            if (groupInfo == null ||
                    GroupStatusEnum.INACTIVE.getStatus().equals(groupInfo.getStatus())) {
                throw new BusinessException("You cannot join this group chat!");
            }
            applyReceiveUserId = groupInfo.getGroupOwnerId();
        } else {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // Get and check if exists one application
        UserContactApply applyRecord = this.userContactApplyMapper.selectUserContactApplyByUniqueKeys(
                userId, applyReceiveUserId, contactorId
        );

        UserContactApply newApply = new UserContactApply();
        if (applyRecord == null) {
            newApply.setApplyUserId(userId);
            newApply.setReceiveUserId(applyReceiveUserId);
            newApply.setContactId(contactorId);
            newApply.setContactType(contactType.getType());
            newApply.setStatus(UserContactApplyStatusEnum.PENDING.getStatus());
            newApply.setApplyInfo(applyMsg);
            newApply.setLastApplyTime(new Date());
            this.userContactApplyMapper.insertUserContactApply(newApply);
        } else {
            newApply.setId(applyRecord.getId());
            newApply.setStatus(UserContactApplyStatusEnum.PENDING.getStatus());
            newApply.setApplyInfo(applyMsg);
            newApply.setLastApplyTime(new Date());
            this.userContactApplyMapper.updateUserContactApply(newApply);
        }

        if (applyRecord == null ||
                !UserContactApplyStatusEnum.PENDING.getStatus().equals(applyRecord.getStatus())) {
            // send ws message
            MessageDTO<? extends Serializable> messageDTO = new MessageDTO<>();
            messageDTO.setMessageType(CommandTypeEnum.CONTACT_APPLY.getType());
            messageDTO.setContent(applyMsg);
            messageDTO.setContactorId(contactorId);
            messageDTO.setContactType(contactType.getType());
            this.messageHandler.sendMessage(messageDTO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processRequest(String userId, Long applyId, Short applyStatus) {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(applyStatus);
        if (statusEnum == UserContactApplyStatusEnum.PENDING) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserContactApply userContactApply = this.userContactApplyMapper.selectUserContactApplyById(applyId);
        if (userContactApply == null || !userId.equals(userContactApply.getReceiveUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        UserContactApply apply = new UserContactApply();
        apply.setStatus(applyStatus);
        apply.setLastApplyTime(new Date());
        UserContactApplyQuery query = new UserContactApplyQuery();
        query.setId(applyId);
        query.setStatus(UserContactApplyStatusEnum.PENDING.getStatus());
        Integer count = this.userContactApplyMapper.updateUserContactApplyByQuery(apply, query);
        if (count != 1) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        // Add new contact when you approve the request
        if (UserContactApplyStatusEnum.APPROVED == statusEnum) {
            this.userContactService.addContact(
                    userContactApply.getApplyUserId(),
                    userContactApply.getReceiveUserId(),
                    userContactApply.getContactId(),
                    userContactApply.getContactType(),
                    userContactApply.getApplyInfo()
            );
        }

        // Black the one when you set blocked
        if (UserContactApplyStatusEnum.BLOCKED == statusEnum) {
            UserContact userContact = new UserContact();
            userContact.setUserId(userContactApply.getContactId());
            userContact.setContactorId(userContactApply.getApplyUserId());
            userContact.setContactType(userContactApply.getContactType());
            userContact.setStatus(UserContactStatusEnum.BLOCKED.getStatus());
            this.userContactMapper.insertOrUpdateUserContact(userContact);
        }
    }
}
