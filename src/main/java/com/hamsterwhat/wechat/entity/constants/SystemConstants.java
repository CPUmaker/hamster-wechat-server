package com.hamsterwhat.wechat.entity.constants;

import com.hamsterwhat.wechat.entity.enums.UserContactTypeEnum;

public class SystemConstants {
    public static final String VERSION = "V1";

    public static final String UPLOAD_FOLDER = "/upload";

    public static final String UPLOAD_AVATAR_FOLDER = "/upload/avatar";

    public static final String UPLOAD_RELEASE_FOLDER = "/upload/release";

    public static final String UPLOAD_FILE_FOLDER = "/upload/file";

    public static final String UPLOAD_IMAGE_FOLDER = "/upload/image";

    public static final String UPLOAD_VIDEO_FOLDER = "/upload/video";

    public static final String COVER_SUFFIX = "_cover";

    public static final String RELEASE_FILE_EXTENSION = ".tar.gz";

    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {".jpeg", ".jpg", ".png", ".gif", ".bmp", ".webp"};

    public static final String[] ALLOWED_VIDEO_EXTENSIONS = {".mp4", ".mp3", ".m4a", ".m4v", ".avi", ".rmvb", ".mkv", ".mov"};

    public static final Integer USER_ID_LENGTH = 11;

    public static final Integer GROUP_ID_LENGTH = 11;

    public static final Integer TOKEN_DIGEST_LENGTH = 20;

    public static final String ROBOT_ID = UserContactTypeEnum.USER.getPrefix() + "robot";

    public static final String DEFAULT_APPLY_MSG = "Hey guys, I am %s. Nice to see you here!";

    public static final String DEFAULT_GROUP_CREATE_MSG = "The group has been created!";

    public static final String DEFAULT_GROUP_DEACTIVATE_MSG = "The group has been deactivated!";

    public static final String DEFAULT_JOIN_GROUP_MSG = "%s has joined the group!";

    public static final String DEFAULT_LEAVE_GROUP_MSG = "%s has left the group!";

    public static final String DEFAULT_REMOVE_FROM_GROUP_MSG = "Admin has kicked %s off from the group!";

    public static final String PASSWORD_REGEX = "^[0-9a-zA-Z~!@#$%^&*_]{8,18}$";
}
