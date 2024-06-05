package com.hamsterwhat.wechat.aspect;

import com.hamsterwhat.wechat.annotation.GlobalInterceptor;
import com.hamsterwhat.wechat.entity.constants.RedisConstants;
import com.hamsterwhat.wechat.entity.dto.TokenUserInfoDTO;
import com.hamsterwhat.wechat.entity.enums.ResponseCodeEnum;
import com.hamsterwhat.wechat.exception.BusinessException;
import com.hamsterwhat.wechat.utils.RedisUtils;
import com.hamsterwhat.wechat.utils.StringUtils;
import com.hamsterwhat.wechat.utils.TokenUserInfoHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Aspect
@Component
public class GlobalOperationAspect {

    private static final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    @Resource
    private RedisUtils redisUtils;

    @Before("@annotation(com.hamsterwhat.wechat.annotation.GlobalInterceptor)")
    public void doBefore(JoinPoint joinPoint) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            GlobalInterceptor annotation = method.getAnnotation(GlobalInterceptor.class);
            if (annotation == null) {
                return;
            }
            if (annotation.checkLogin()) {
                this.checkLogin();
                if (annotation.checkAdmin()) {
                    this.checkAdmin();
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Global Interceptor Error", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void checkLogin() {
        // Get RequestAttributes from Spring ThreadLocal
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        // Get token digest from request header
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        // Fetch token DTO from Redis
        String tokenKey = RedisConstants.WS_TOKEN_KEY + token;
        TokenUserInfoDTO tokenUserInfoDTO = (TokenUserInfoDTO) redisUtils.get(tokenKey);
        if (tokenUserInfoDTO == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }

        // Save DTO into ThreadLocal
        TokenUserInfoHolder.saveTokenUserInfo(tokenUserInfoDTO);
    }

    private void checkAdmin() {
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoHolder.getTokenUserInfo();
        if (Boolean.FALSE.equals(tokenUserInfoDTO.getIsAdmin())) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }
}
