package com.hamsterwhat.wechat.controller;

import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.entity.vo.ResponseVO;
import com.hamsterwhat.wechat.exception.BusinessException;

public class BaseController {

    protected static final String STATUS_SUCCESS = "success";

    protected static final String STATUS_ERROR = "error";

    protected <T> ResponseVO<T> getSuccessResponse(T data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setStatus(STATUS_SUCCESS);
        response.setCode(ResponseCodeEnum.CODE_200.getCode());
        response.setMsg(ResponseCodeEnum.CODE_200.getMessage());
        response.setData(data);
        return response;
    }

    protected <T> ResponseVO<T> getBusinessErrorResponse(BusinessException e, T data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setStatus(STATUS_ERROR);
        response.setCode(e.getCode() != null ? e.getCode() : ResponseCodeEnum.CODE_400.getCode());
        response.setMsg(e.getMessage());
        response.setData(data);
        return response;
    }

    protected <T> ResponseVO<T> getServerErrorResponse(T data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setStatus(STATUS_ERROR);
        response.setCode(ResponseCodeEnum.CODE_500.getCode());
        response.setMsg(ResponseCodeEnum.CODE_500.getMessage());
        response.setData(data);
        return response;
    }
}
