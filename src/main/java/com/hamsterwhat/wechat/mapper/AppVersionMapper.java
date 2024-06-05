package com.hamsterwhat.wechat.mapper;

import com.hamsterwhat.wechat.entity.po.AppVersion;
import com.hamsterwhat.wechat.entity.query.AppVersionQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppVersionMapper {
    AppVersion selectAppVersionById(Long id);
    
    AppVersion selectAppVersionByVersion(String version);

    AppVersion selectLatestUpdate(@Param("version") String version, @Param("uid") String uid);
    
    List<AppVersion> selectAppVersionList();

    List<AppVersion> selectAppVersionListByQuery(@Param("query") AppVersionQuery query);

    Integer selectAppVersionCountByQuery(@Param("query") AppVersionQuery query);

    Integer insertAppVersion(AppVersion appVersion);

    Integer updateAppVersion(AppVersion appVersion);

    Integer deleteAppVersionById(Long id);
}
