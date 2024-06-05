package com.hamsterwhat.wechat.utils;

import com.hamsterwhat.wechat.exception.BusinessException;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CopyUtils {


    public static <T, S> List<T> copyList(List<S> sourceList, Class<T> targetClass) {
        List<T> targetList = new ArrayList<>();
        for (S s : sourceList) {
            T targetElement;
            try {
                targetElement = targetClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new BusinessException(e.getMessage());
            }
            BeanUtils.copyProperties(s, targetElement);
            targetList.add(targetElement);
        }
        return targetList;
    }

    public static <T, S> T copy(S source, Class<T> targetClass) {
        T targetElement;
        try {
            targetElement = targetClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
        BeanUtils.copyProperties(source, targetElement);
        return targetElement;
    }
}
