package com.hamsterwhat.wechat.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SystemSettingsDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 9108568666981466918L;

    private Integer maxGroupCount = 5;

    private Integer maxGroupMemberCount = 500;

    private Long maxImageSizeInMegaBytes = 2L;

    private Long maxVideoSizeInMegaBytes = 5L;

    private Long maxFileSizeInMegaBytes = 5L;

    private String robotId = SystemConstants.ROBOT_ID;

    private String robotNickName = "Hamster";

    private String robotWelcome = "Welcome to Hamster! This is a friendly community.";
}
