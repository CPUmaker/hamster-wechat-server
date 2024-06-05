package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.BindException;

@RestControllerAdvice
public class GlobalExceptionHandlerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandlerController.class);

    @ExceptionHandler(Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("Exception happened when requesting at {}", request.getRequestURL(), e);
        ResponseVO<Object> ajaxResponse = new ResponseVO<>();
        if (e instanceof NoHandlerFoundException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_404.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_404.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof BusinessException bizException) {
            // 业务错误
            ajaxResponse.setCode(bizException.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : bizException.getCode());
            ajaxResponse.setMsg(bizException.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof BindException || e instanceof MethodArgumentNotValidException) {
            // 参数类型错误
            ajaxResponse.setCode(ResponseCodeEnum.CODE_600.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_600.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            // 主键冲突
            ajaxResponse.setCode(ResponseCodeEnum.CODE_601.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_601.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        } else {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_500.getCode());
            ajaxResponse.setMsg(ResponseCodeEnum.CODE_500.getMessage());
            ajaxResponse.setStatus(STATUS_ERROR);
        }
        return ajaxResponse;
    }
}
