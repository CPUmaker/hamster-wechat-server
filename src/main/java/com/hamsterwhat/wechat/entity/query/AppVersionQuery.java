package com.hamsterwhat.wechat.entity.query;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AppVersionQuery extends BaseParam {
    private Long id;
    private Long idNotEqual;

    private String version;
    private String versionLessThan;
    private String versionGreaterThan;

    private String description;

    private Short status;

    private String grayscaleUID;

    private Short fileType;

    private String backlink;

    private Date createdAt;
    private Date createdAtStart;
    private Date createdAtENd;
}
