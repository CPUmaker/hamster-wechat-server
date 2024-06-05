package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.po.UserContactApply;
import com.hamsterwhat.wechat.entity.query.UserContactApplyQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.service.UserContactApplyService;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/friend-requests")
public class UserContactApplyController extends BaseController {

    private final UserContactApplyService userContactApplyService;

    @Autowired
    public UserContactApplyController(UserContactApplyService userContactApplyService) {
        this.userContactApplyService = userContactApplyService;
    }

    @GetMapping
    @GlobalInterceptor
    public ResponseVO<Object> getNewRequests(Integer page, Integer size) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();

        UserContactApplyQuery query = new UserContactApplyQuery();
        query.setReceiveUserId(token.getUserId());
        query.setLimit(size);
        query.setOffset(page * size);
        query.setOrderBy("last_apply_time");
        query.setOrderDirection("DESC");
        PaginationResultVO<UserContactApply> result = this.userContactApplyService.findPageByParam(query);

        return getSuccessResponse(result);
    }

    @PostMapping
    @GlobalInterceptor
    public ResponseVO<Object> apply(@NotEmpty String contactorId, String applyMsg) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.userContactApplyService.createRequest(
                token.getUserId(), token.getUsername(), contactorId, applyMsg);
        return getSuccessResponse(null);
    }

    @PatchMapping("/{applyId}")
    @GlobalInterceptor
    public ResponseVO<Object> processRequest(@PathVariable Long applyId, @NotNull Short status) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.userContactApplyService.processRequest(token.getUserId(), applyId, status);
        return getSuccessResponse(null);
    }
}
