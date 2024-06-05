package com.hamsterwhat.wechat.service;

import com.hamsterwhat.wechat.entity.po.AppVersion;
import com.hamsterwhat.wechat.entity.query.AppVersionQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AppVersionService {
    List<AppVersion> findListByParam(AppVersionQuery param);

    PaginationResultVO<AppVersion> findPageByParam(AppVersionQuery param);

    Integer add(AppVersion appVersion);

    Integer update(AppVersion appVersion);

    Integer deleteById(Long id);

    void addAppVersion(AppVersion appVersion, MultipartFile file);

    void updateAppVersion(AppVersion appVersion, MultipartFile file);

    AppVersion getLatestAppVersion(String version, String userId);
}
