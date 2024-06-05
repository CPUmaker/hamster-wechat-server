package com.hamsterwhat.wechat.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Convert an object to a JSON string
     * @param object the object to be converted to JSON
     * @return JSON string representation of the object
     */
    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    /**
     * Convert a JSON string to an object of the specified class
     * @param json the JSON string to be converted
     * @param clazz the class of the object to be returned
     * @param <T> the type of the object to be returned
     * @return the object converted from the JSON string
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }
}
