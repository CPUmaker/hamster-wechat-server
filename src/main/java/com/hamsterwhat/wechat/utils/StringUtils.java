package com.hamsterwhat.wechat.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Arrays;

public class StringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    public static String getRandomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static String getRandomNumber(int length) {
        return RandomStringUtils.randomNumeric(length);
    }

    public static String encodeMD5(String origin) {
        return DigestUtils.md5Hex(origin);
    }

    public static String encodeMD5(String[] origin) {
        return DigestUtils.md5Hex(String.join("", origin));
    }

    /**
     * Generate session id for user-type chat session
     * @param userId The param represents id of left-side user in the relationship
     * @param contactorId The param represents id of right-side user in the relationship
     * @return MD5 represent of sorted concat of the relationship
     */
    public static String getSessionId(String userId, String contactorId) {
        String[] ids = {userId, contactorId};
        Arrays.sort(ids);
        return StringUtils.encodeMD5(ids);
    }

    /**
     * Generate session id for group-type chat session
     * @param groupId The param represents id of the group
     * @return MD5 represent of {@code groupId}
     */
    public static String getSessionId(String groupId) {
        return StringUtils.encodeMD5(groupId);
    }

    public static String cleanHtmlTag(String content) {
        content = content
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\r\n", "<br/>")
                .replaceAll("\n", "<br/>");
        return content;
    }

    public static String getFileSuffix(String fileName) {
        if (isEmpty(fileName)) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
