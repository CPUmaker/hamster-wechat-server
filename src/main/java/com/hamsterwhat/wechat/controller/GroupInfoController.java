package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.enums.UserContactStatusEnum;
import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.query.BaseParam;
import com.hamsterwhat.wechat.entity.query.GroupInfoQuery;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.entity.vo.GroupInfoVO;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.service.GroupInfoService;
import com.hamsterwhat.wechat.service.UserContactService;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/groups")
@Validated
public class GroupInfoController extends BaseController {

    private final GroupInfoService groupInfoService;

    private final UserContactService userContactService;

    @Autowired
    public GroupInfoController(GroupInfoService groupInfoService, UserContactService userContactService) {
        this.groupInfoService = groupInfoService;
        this.userContactService = userContactService;
    }

    @GetMapping
    @GlobalInterceptor
    public ResponseVO<List<GroupInfo>> getGroupInfoList() {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();

        GroupInfoQuery query = new GroupInfoQuery();
        query.setGroupOwnerId(tokenUserInfoDTO.getUserId());
        query.setOrderBy("created_at");
        query.setOrderDirection("DESC");
        List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(query);

        return getSuccessResponse(groupInfoList);
    }

    @GetMapping("/{groupId}")
    @GlobalInterceptor
    public ResponseVO<GroupInfoVO> getGroupInfo(@PathVariable String groupId) {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();

        GroupInfo groupInfo = this.groupInfoService.getGroupInfo(tokenUserInfoDTO.getUserId(), groupId);
        UserContactQuery query = new UserContactQuery();
        query.setContactorId(groupId);
        Integer memberCount = this.userContactService.findCountByParam(query);

        GroupInfoVO result = new GroupInfoVO();
        result.setGroupInfo(groupInfo);
        result.setMemberCount(memberCount);

        return getSuccessResponse(result);
    }

    @GetMapping("/{groupId}/members")
    @GlobalInterceptor
    public ResponseVO<GroupInfoVO> getGroupChats(@PathVariable String groupId) {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();

        GroupInfo groupInfo = this.groupInfoService.getGroupInfo(tokenUserInfoDTO.getUserId(), groupId);

        UserContactQuery query = new UserContactQuery();
        query.setContactorId(groupId);
        query.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        query.setOrderBy("uc.created_at");
        query.setOrderDirection("ASC");
        query.setIncludesUser(BaseParam.TableJoinType.INNER);
        List<UserContact> userContactList = this.userContactService.findListByParam(query);

        GroupInfoVO result = new GroupInfoVO();
        result.setGroupInfo(groupInfo);
        result.setUserContacts(userContactList);

        return getSuccessResponse(result);
    }

    @PostMapping
    @GlobalInterceptor
    public ResponseVO<Object> saveGroup(
            @NotEmpty String groupName,
            String groupNotice,
            MultipartFile groupImg
    ) {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupOwnerId(tokenUserInfoDTO.getUserId());
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        this.groupInfoService.saveGroup(groupInfo, groupImg);

        return getSuccessResponse(null);
    }

    @PostMapping("/{groupId}/leave")
    @GlobalInterceptor
    public ResponseVO<Object> leaveGroup(@PathVariable String groupId) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.groupInfoService.leaveGroup(token.getUserId(), groupId);
        return getSuccessResponse(null);
    }

    @PutMapping("/{groupId}")
    @GlobalInterceptor
    public ResponseVO<Object> updateGroup(
            @PathVariable String groupId,
            @NotEmpty String groupName,
            String groupNotice,
            MultipartFile groupImg
    ) {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();

        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);
        groupInfo.setGroupOwnerId(tokenUserInfoDTO.getUserId());
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        this.groupInfoService.updateGroup(groupInfo, groupImg);

        return getSuccessResponse(null);
    }

    @DeleteMapping("/{groupId}")
    @GlobalInterceptor
    public ResponseVO<Object> deactivateGroup(@PathVariable String groupId) {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();
        this.groupInfoService.deactivateGroup(tokenUserInfoDTO.getUserId(), groupId);
        return getSuccessResponse(null);
    }

    @PostMapping("/{groupId}/members")
    @GlobalInterceptor
    public ResponseVO<Object> addGroupMember(@PathVariable String groupId, @NotEmpty String userId) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.groupInfoService.addGroupMember(token.getUserId(), groupId, userId);
        return getSuccessResponse(null);
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @GlobalInterceptor
    public ResponseVO<Object> deleteGroupMember(
            @PathVariable String groupId,
            @PathVariable String userId
    ) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.groupInfoService.deleteGroupMember(token.getUserId(), groupId, userId);
        return getSuccessResponse(null);
    }
}
