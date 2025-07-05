package com.webcontainer.stage4;

import javax.servlet.http.HttpSession;

import com.webcontainer.stage3.HttpServletRequestImpl;

/**
 * 第四阶段：增强的HttpServletRequest实现
 * 支持会话管理
 */
public class EnhancedHttpServletRequestImpl extends HttpServletRequestImpl {
    private final SessionManager sessionManager;
    private final EnhancedHttpServletResponseImpl response;

    public EnhancedHttpServletRequestImpl(String method, String requestURI, String protocol,
            SessionManager sessionManager,
            EnhancedHttpServletResponseImpl response) {
        super(method, requestURI, protocol);
        this.sessionManager = sessionManager;
        this.response = response;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return sessionManager.getSession(this, response, create);
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String getRequestedSessionId() {
        // 从Cookie中获取会话ID
        javax.servlet.http.Cookie[] cookies = getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        String sessionId = getRequestedSessionId();
        if (sessionId == null) {
            return false;
        }

        HttpSession session = sessionManager.getSession(this, response, false);
        return session != null && sessionId.equals(session.getId());
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return getRequestedSessionId() != null;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false; // 简化实现，暂不支持URL重写
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public javax.servlet.http.Cookie[] getCookies() {
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader == null || cookieHeader.trim().isEmpty()) {
            return new javax.servlet.http.Cookie[0];
        }

        java.util.List<javax.servlet.http.Cookie> cookies = new java.util.ArrayList<>();
        String[] cookiePairs = cookieHeader.split(";");

        for (String cookiePair : cookiePairs) {
            String[] parts = cookiePair.trim().split("=", 2);
            if (parts.length == 2) {
                cookies.add(new javax.servlet.http.Cookie(parts[0].trim(), parts[1].trim()));
            }
        }

        return cookies.toArray(new javax.servlet.http.Cookie[0]);
    }

    /**
     * 获取SessionManager实例（用于会话统计等功能）
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
}
