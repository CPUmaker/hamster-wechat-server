package com.hamsterwhat.wechat.utils;

import com.hamsterwhat.wechat.entity.constants.SystemConstants;
import com.hamsterwhat.wechat.exception.BusinessException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component
public class UploadUtils {
    public static void uploadAvatar(String path, String fileNameWithoutExtension, MultipartFile avatar) {
        File avatarFolder = new File(path);
        if (!avatarFolder.exists() && !avatarFolder.mkdirs()) {
            throw new BusinessException("Unable to create avatar folder.");
        }
        String imgExtension = FilenameUtils.getExtension(avatar.getOriginalFilename());
        String avatarUploadFileName = fileNameWithoutExtension + FilenameUtils.EXTENSION_SEPARATOR + imgExtension;
        String avatarUploadPath = FilenameUtils.concat(avatarFolder.getPath(), avatarUploadFileName);
        try {
            avatar.transferTo(new File(avatarUploadPath));
        } catch (IOException e) {
            throw new BusinessException("Failed to upload avatar file.");
        }
    }

    public static void uploadRelease(String path, String fileNameWithoutExtension, MultipartFile file) {
        File releaseFolder = new File(path);
        if (!releaseFolder.exists() && !releaseFolder.mkdirs()) {
            throw new BusinessException("Unable to create release folder.");
        }
        String fileName = fileNameWithoutExtension + SystemConstants.RELEASE_FILE_EXTENSION;
        String releaseUploadPath = FilenameUtils.concat(releaseFolder.getPath(), fileName);
        try {
            file.transferTo(new File(releaseUploadPath));
        } catch (IOException e) {
            throw new BusinessException("Failed to upload release file.");
        }
    }

    public static void uploadFile(String path, String fileName, MultipartFile file) {
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            throw new BusinessException("Unable to create chat file upload folder.");
        }
        String releaseUploadPath = FilenameUtils.concat(folder.getPath(), fileName);
        try {
            file.transferTo(new File(releaseUploadPath));
        } catch (IOException e) {
            throw new BusinessException("Failed to upload file.");
        }
    }

    private UploadUtils() {}
}
