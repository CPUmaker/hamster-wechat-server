package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.po.UserContactApply;
import com.hamsterwhat.wechat.entity.query.UserContactApplyQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;

public interface UserContactApplyService {

    PaginationResultVO<UserContactApply> findPageByParam(UserContactApplyQuery param);

    void createRequest(String userId, String username, String contactorId, String applyMsg);

    void processRequest(String userId, Long applyId, Short applyStatus);
}
