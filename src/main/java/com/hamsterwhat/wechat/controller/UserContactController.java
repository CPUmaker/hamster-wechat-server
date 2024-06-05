package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.dto.UserContactSearchResultDTO;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;
import com.hamsterwhat.wechat.entity.po.UserContact;
import com.hamsterwhat.wechat.entity.po.UserContactApply;
import com.hamsterwhat.wechat.entity.query.BaseParam;
import com.hamsterwhat.wechat.entity.query.UserContactApplyQuery;
import com.hamsterwhat.wechat.entity.query.UserContactQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.service.UserContactApplyService;
import com.hamsterwhat.wechat.service.UserContactService;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contacts")
@Validated
public class UserContactController extends BaseController {

    private final UserContactService userContactService;

    @Autowired
    public UserContactController(
            UserContactService userContactService
    ) {
        this.userContactService = userContactService;
    }

    @GetMapping("/search")
    @GlobalInterceptor
    public ResponseVO<UserContactSearchResultDTO> search(@RequestParam("contactorId") String contactorId) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        UserContactSearchResultDTO result = this.userContactService.searchContact(
                token.getUserId(), contactorId
        );
        return getSuccessResponse(result);
    }

    @GetMapping
    @GlobalInterceptor
    public ResponseVO<Object> getAllContacts(@RequestParam("contactType") Short contactType) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        List<UserContact> result = this.userContactService.getAllContacts(
                token.getUserId(), contactType
        );
        return getSuccessResponse(result);
    }

    @DeleteMapping("/{contactorId}")
    @GlobalInterceptor
    public ResponseVO<Object> deleteContactor(@PathVariable String contactorId, @NotNull Boolean isToBlock) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        Integer count = this.userContactService.deleteContact(
                token.getUserId(), contactorId, isToBlock
        );
        if (count == 0) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        return getSuccessResponse(null);
    }
}
