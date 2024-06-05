package com.hamsterwhat.wechat.utils;

import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class DownloadUtils {

    private static final Logger logger = LoggerFactory.getLogger(DownloadUtils.class);

    public static void downloadFile(Path filePath, HttpServletResponse response) {
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new BusinessException(ResponseCodeEnum.CODE_602);
            }
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"");
            response.setHeader("Content-Length", String.valueOf(resource.contentLength()));

            try (InputStream inputStream = resource.getInputStream();
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            logger.error("Download file error", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }
}
