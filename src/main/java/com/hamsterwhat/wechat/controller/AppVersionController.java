package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.po.AppVersion;
import com.hamsterwhat.wechat.entity.query.AppVersionQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.service.AppVersionService;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/app-version")
public class AppVersionController extends BaseController {

    private final AppVersionService appVersionService;

    @Autowired
    public AppVersionController(AppVersionService appVersionService) {
        this.appVersionService = appVersionService;
    }

    @PostMapping
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<Object> addAppVersion(AppVersion appVersion, MultipartFile file) {
        this.appVersionService.addAppVersion(appVersion, file);
        return getSuccessResponse(null);
    }

    @GetMapping
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<Object> getAllAppVersion(AppVersionQuery query) {
        query.setOrderBy("id");
        query.setOrderDirection("DESC");
        PaginationResultVO<AppVersion> result = this.appVersionService.findPageByParam(query);
        return getSuccessResponse(result);
    }

    @GetMapping("/check")
    @GlobalInterceptor
    public ResponseVO<Object> checkAppVersion(String version) {
        TokenUserInfoDTO token = TokenUserInfoHolder.getTokenUserInfo();
        AppVersion appVersion = this.appVersionService.getLatestAppVersion(version, token.getUserId());
        return getSuccessResponse(appVersion);
    }

    @PatchMapping("/{id}")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<Object> updateAppVersion(
            @PathVariable("id") Long id, AppVersion appVersion, MultipartFile file) {
        appVersion.setId(id);
        this.appVersionService.updateAppVersion(appVersion, file);
        return getSuccessResponse(null);
    }

    @DeleteMapping("/{id}")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO<Object> deleteAppVersion(@PathVariable("id") Long id) {
        this.appVersionService.deleteById(id);
        return getSuccessResponse(null);
    }
}
