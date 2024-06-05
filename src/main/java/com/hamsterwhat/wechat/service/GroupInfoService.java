package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.query.GroupInfoQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GroupInfoService {

    GroupInfo getGroupInfoById(String id);

    GroupInfo getGroupInfoByGroupId(String groupId);

    List<GroupInfo> findListByParam(GroupInfoQuery param);

    Integer findCountByParam(GroupInfoQuery param);

    PaginationResultVO<GroupInfo> findPageByParam(GroupInfoQuery param);

    Integer add(GroupInfo userInfo);

    Integer addBatch(List<GroupInfo> list);

    Integer update(GroupInfo userInfo);

    Integer updateBatch(List<GroupInfo> list);

    Integer addAndUpdateBatch(List<GroupInfo> list);

    Integer deleteById(Long id);

    Integer deleteByGroupId(String groupId);

    Integer deleteByGroupOwnerId(String groupOwnerId);

    Integer deleteGroupInfoByIds(Long[] ids);

    /**
     * Service to create a new group
     * @param groupInfo includes fields of group
     * @param groupImg group cover image
     */
    void saveGroup(GroupInfo groupInfo, MultipartFile groupImg);

    /**
     * Service to update fields for existing group
     * @param groupInfo includes fields of group
     * @param groupImg group cover image
     */
    void updateGroup(GroupInfo groupInfo, MultipartFile groupImg);

    void deactivateGroup(String userId, String groupId);

    void leaveGroup(String userId, String groupId);

    GroupInfo getGroupInfo(String userId, String groupId);

    void addGroupMember(String userId, String groupId, String memberId);

    void deleteGroupMember(String userId, String groupId, String memberId);
}
