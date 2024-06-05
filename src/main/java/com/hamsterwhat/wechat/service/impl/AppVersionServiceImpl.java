package com.hamsterwhat.wechat.service.impl;

import com.hamsterwhat.wechat.entity.constants.AppProperties;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.enums.AppVersionFileTypeEnum;
import com.hamsterwhat.wechat.entity.enums.AppVersionStatusEnum;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.entity.po.AppVersion;
import com.hamsterwhat.wechat.entity.query.AppVersionQuery;
import com.hamsterwhat.wechat.entity.vo.PaginationResultVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.mapper.AppVersionMapper;
import com.hamsterwhat.wechat.service.AppVersionService;
import com.hamsterwhat.wechat.utils.UploadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AppVersionServiceImpl implements AppVersionService {

    private final AppVersionMapper appVersionMapper;

    private final AppProperties appProperties;

    @Autowired
    public AppVersionServiceImpl(AppVersionMapper appVersionMapper, AppProperties appProperties) {
        this.appVersionMapper = appVersionMapper;
        this.appProperties = appProperties;
    }

    @Override
    public List<AppVersion> findListByParam(AppVersionQuery param) {
        if (param == null) {
            return this.appVersionMapper.selectAppVersionList();
        }
        return this.appVersionMapper.selectAppVersionListByQuery(param);
    }

    @Override
    public PaginationResultVO<AppVersion> findPageByParam(AppVersionQuery param) {
        Integer pageSize = param.getPageSize();
        Integer currentPage = param.getPage();

        List<AppVersion> versionList = this.appVersionMapper.selectAppVersionListByQuery(param);

        param.setPage(null);
        param.setPageSize(null);
        Integer totalCount = this.appVersionMapper.selectAppVersionCountByQuery(param);

        PaginationResultVO<AppVersion> result = new PaginationResultVO<>();
        result.setTotalCount(totalCount);
        result.setTotalPage(totalCount / pageSize);
        result.setCurrentPage(currentPage);
        result.setPageSize(pageSize);
        result.setList(versionList);
        return result;
    }

    @Override
    public Integer add(AppVersion appVersion) {
        this.appVersionMapper.insertAppVersion(appVersion);
        return 1;
    }

    @Override
    public Integer update(AppVersion appVersion) {
        this.appVersionMapper.updateAppVersion(appVersion);
        return 1;
    }

    @Override
    public Integer deleteById(Long id) {
        this.appVersionMapper.deleteAppVersionById(id);
        return 1;
    }

    @Override
    public void addAppVersion(AppVersion appVersion, MultipartFile file) {
        validateAppVersionRecord(appVersion, file);
        appVersion.setStatus(AppVersionStatusEnum.NOT_RELEASED.getType());
        appVersion.setId(null);
        this.appVersionMapper.insertAppVersion(appVersion);
    }

    @Override
    public void updateAppVersion(AppVersion appVersion, MultipartFile file) {
        validateAppVersionRecord(appVersion, file);
        this.appVersionMapper.updateAppVersion(appVersion);
    }

    @Override
    public AppVersion getLatestAppVersion(String version, String userId) {
        return this.appVersionMapper.selectLatestUpdate(version, userId);
    }

    private void validateAppVersionRecord(AppVersion appVersion, MultipartFile file) {
        AppVersionFileTypeEnum fileTypeEnum = AppVersionFileTypeEnum.getByType(appVersion.getFileType());

        // Ensure change only occurs when the version is not released
        if (appVersion.getId() != null) {
            AppVersion oldAppVersion = this.appVersionMapper.selectAppVersionById(appVersion.getId());
            if (oldAppVersion == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            if (!AppVersionStatusEnum.NOT_RELEASED.getType().equals(oldAppVersion.getStatus())) {
                throw new BusinessException("You can not change the version has already been released!");
            }
        }

        // Ensure uniqueness of version in database
        AppVersionQuery query = new AppVersionQuery();
        query.setVersion(appVersion.getVersion());
        query.setIdNotEqual(appVersion.getId());
        Integer count = this.appVersionMapper.selectAppVersionCountByQuery(query);
        if (count > 0) {
            throw new BusinessException("The version number already exists!");
        }

        // Ensure each type of file has corresponding params
        if (fileTypeEnum == AppVersionFileTypeEnum.LOCAL) {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            this.uploadReleaseFile(appVersion, file);
        } else if (fileTypeEnum == AppVersionFileTypeEnum.BACKLINK) {
            if (appVersion.getBacklink() == null) {
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
        }
    }

    private void uploadReleaseFile(AppVersion appVersion, MultipartFile file) {
        String avatarFolderPath = this.appProperties.getProject().getFolder() +
                SystemConstants.UPLOAD_RELEASE_FOLDER;
        UploadUtils.uploadRelease(avatarFolderPath, appVersion.getVersion(), file);
    }
}
