package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.UserContactApply;
import com.hamsterwhat.wechat.entity.query.UserContactApplyQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserContactApplyMapper {
    UserContactApply selectUserContactApplyById(Long id);

    UserContactApply selectUserContactApplyByUniqueKeys(
            @Param("applyUserId") String applyUserId,
            @Param("receiveUserId") String receiveUserId,
            @Param("contactId") String contactId
    );

    List<UserContactApply> selectUserContactApplyList();

    List<UserContactApply> selectUserContactApplyListByQuery(@Param("query") UserContactApplyQuery query);

    Integer selectUserContactApplyCountByQuery(@Param("query") UserContactApplyQuery query);

    Integer insertUserContactApply(UserContactApply userContactApply);

    Integer updateUserContactApply(UserContactApply userContactApply);

    Integer updateUserContactApplyByQuery(
            @Param("apply") UserContactApply userContactApply,
            @Param("query") UserContactApplyQuery query
    );

    Integer deleteUserContactApplyById(Long id);

    Integer deleteUserContactApplyByIds(@Param("ids") Long[] ids);

    Integer deleteUserContactApplyByQuery(@Param("query") UserContactApplyQuery query);
}
