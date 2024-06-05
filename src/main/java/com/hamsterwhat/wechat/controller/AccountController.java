package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.SystemSettingsDTO;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.po.UserInfo;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.entity.vo.UserInfoVO;
import com.hamsterwhat.wechat.service.UserInfoService;
import com.hamsterwhat.wechat.utils.CopyUtils;
import com.hamsterwhat.wechat.utils.SystemSettings;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/account")
@Validated
public class AccountController extends BaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private SystemSettings systemSettings;

    @PostMapping("/register")
    public ResponseVO<Object> register(
            @NotEmpty @Email String email,
            @NotEmpty @Pattern(regexp = SystemConstants.PASSWORD_REGEX) String password,
            @NotEmpty String username
    ) {
        this.userInfoService.register(email, password, username);
        return getSuccessResponse(null);
    }

    @PostMapping("/login")
    public ResponseVO<UserInfoVO> login(
            @NotEmpty @Email String email,
            @NotEmpty String password
    ) {
        UserInfoVO result = this.userInfoService.login(email, password);

        return getSuccessResponse(result);
    }

    @PostMapping("/reset-password")
    @GlobalInterceptor
    public ResponseVO<Object> resetPassword(
            @NotEmpty String originPassword,
            @NotEmpty @Pattern(regexp = SystemConstants.PASSWORD_REGEX) String newPassword
    ) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.userInfoService.resetPassword(token.getUserId(), originPassword, newPassword);
        // TODO: enforce logout
        return getSuccessResponse(null);
    }

    @PostMapping("/logout")
    @GlobalInterceptor
    public ResponseVO<Object> logout() {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        this.userInfoService.logout(token.getUserId());
        return getSuccessResponse(null);
    }

    @GetMapping("/my-info")
    @GlobalInterceptor
    public ResponseVO<UserInfoVO> getMyInfo() {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(token.getUserId());
        UserInfoVO result = CopyUtils.copy(userInfo, UserInfoVO.class);
        return getSuccessResponse(result);
    }

    @GetMapping("/sys-settings")
    public ResponseVO<SystemSettingsDTO> getSystemSettings() {
        return getSuccessResponse(this.systemSettings.getSystemSettings());
    }

    @GetMapping("/{userId}")
    public ResponseVO<UserInfo> getUserInfo(@PathVariable String userId) {
        UserInfo userInfo = this.userInfoService.getUserInfoByUserId(userId);
        return getSuccessResponse(userInfo);
    }

    @PatchMapping
    @GlobalInterceptor
    public ResponseVO<Object> updateUserInfo(
            @Email String email,
            String username,
            Short gender,
            String bio,
            String geo,
            MultipartFile avatar
    ) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(token.getUserId());
        userInfo.setEmail(email);
        userInfo.setUsername(username);
        userInfo.setGender(gender);
        userInfo.setBio(bio);
        userInfo.setGeo(geo);
        this.userInfoService.updateUserInfo(userInfo, avatar, token);
        return getSuccessResponse(null);
    }
}
