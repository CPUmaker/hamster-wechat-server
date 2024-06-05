package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.GroupInfo;
import com.hamsterwhat.wechat.entity.query.GroupInfoQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GroupInfoMapper {
    GroupInfo selectGroupInfoById(Long id);

    GroupInfo selectGroupInfoByGroupId(String groupId);

    List<GroupInfo> selectGroupInfoList();

    List<GroupInfo> selectGroupInfoListByGroupOwnerId(String groupOwnerId);

    List<GroupInfo> selectGroupInfoListByQuery(@Param("query") GroupInfoQuery query);

    Integer selectGroupInfoCountByQuery(@Param("query") GroupInfoQuery query);

    Integer insertGroupInfo(GroupInfo groupInfo);

    Integer updateGroupInfo(GroupInfo groupInfo);

    Integer deleteGroupInfoById(Long id);

    Integer deleteGroupInfoByGroupId(String groupId);

    Integer deleteGroupInfoByGroupOwnerId(String groupOwnerId);

    Integer deleteGroupInfoByIds(@Param("ids") Long[] ids);

    Integer deleteGroupInfoByQuery(@Param("query") GroupInfoQuery query);
}
