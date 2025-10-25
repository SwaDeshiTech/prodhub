package com.swadeshitech.prodhub.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.swadeshitech.prodhub.constant.Constants;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class UserIdentityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        Optional<String> userId = getUserIdFromCookie(httpRequest);

        if (userId.isEmpty()) {
            userId = getUserIdFromHeader(httpRequest);
        }

        log.info("UserIdentityFilter triggered for path: {}", httpRequest.getRequestURI());
        log.info("User ID present: {}", userId.isPresent());

        // Store the user ID in the request context only if present
        userId.ifPresent(uid -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                attributes.setAttribute(Constants.USER_ID_CONTEXT_NAME, uid, ServletRequestAttributes.SCOPE_REQUEST);
            }
            ContextHolder.setContext("uuid", uid);
        });

        chain.doFilter(request, response);
    }

    private Optional<String> getUserIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> Constants.USER_ID_COOKIE_NAME.equalsIgnoreCase(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
        }
        return Optional.empty();
    }

    private Optional<String> getUserIdFromHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(Constants.USER_ID_COOKIE_NAME));
    }
}

