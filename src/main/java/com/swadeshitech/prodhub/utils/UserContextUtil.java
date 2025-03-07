package com.swadeshitech.prodhub.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.swadeshitech.prodhub.constant.Constants;

public class UserContextUtil {

    public static String getUserIdFromRequestContext() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return (String) attributes.getAttribute(Constants.USER_ID_CONTEXT_NAME, ServletRequestAttributes.SCOPE_REQUEST);
        }
        return null;
    }
}