package com.hamsterwhat.wechat.mapper;

public interface AppGrayscaleMapper {
    String[] selectGrayscaleUIDsByAppVersionId(Integer appVersionId);
}
