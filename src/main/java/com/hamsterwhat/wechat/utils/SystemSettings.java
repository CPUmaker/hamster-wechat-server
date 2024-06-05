package com.hamsterwhat.wechat.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.entity.dto.SystemSettingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SystemSettings {

    private static final Logger logger = LoggerFactory.getLogger(SystemSettings.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final RedisUtils redisUtils;

    @Autowired
    public SystemSettings(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    public SystemSettingsDTO getSystemSettings() {
        try {
            String key = RedisConstants.SYSTEM_SETTINGS_KEY + SystemConstants.VERSION;
            String json = (String) redisUtils.get(key);
            return StringUtils.isEmpty(json) ?
                    new SystemSettingsDTO() : objectMapper.readValue(json, SystemSettingsDTO.class);
        } catch (JsonProcessingException e) {
            logger.error("System Settings Json Parse Error", e);
            return new SystemSettingsDTO();
        }

    }

    public boolean isExceedGroupCountLimit(Integer groupCount) {
        SystemSettingsDTO systemSettings = getSystemSettings();
        return groupCount >= systemSettings.getMaxGroupCount();
    }

    public boolean isExceedGroupMemberCountLimit(Integer groupCount) {
        SystemSettingsDTO systemSettings = getSystemSettings();
        return groupCount >= systemSettings.getMaxGroupMemberCount();
    }

    public boolean isExceedFileSizeLimit(Long fileSize) {
        SystemSettingsDTO systemSettings = getSystemSettings();
        return fileSize >= systemSettings.getMaxFileSizeInMegaBytes() * 1024 * 1024;
    }

    public boolean isExceedImageSizeLimit(Long fileSize) {
        SystemSettingsDTO systemSettings = getSystemSettings();
        return fileSize >= systemSettings.getMaxImageSizeInMegaBytes() * 1024 * 1024;
    }

    public boolean isExceedVideoSizeLimit(Long fileSize) {
        SystemSettingsDTO systemSettings = getSystemSettings();
        return fileSize >= systemSettings.getMaxVideoSizeInMegaBytes() * 1024 * 1024;
    }
}
