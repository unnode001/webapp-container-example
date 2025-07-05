package com.webcontainer.stage4;

import com.webcontainer.stage3.HttpServletResponseImpl;

/**
 * 第四阶段：增强的HttpServletResponse实现
 * 继承第三阶段的实现，支持更好的Cookie管理
 */
public class EnhancedHttpServletResponseImpl extends HttpServletResponseImpl {

    public EnhancedHttpServletResponseImpl() {
        super();
    }

    @Override
    public void addCookie(javax.servlet.http.Cookie cookie) {
        super.addCookie(cookie);

        // 构建Cookie头
        StringBuilder cookieHeader = new StringBuilder();
        cookieHeader.append(cookie.getName()).append("=").append(cookie.getValue());

        if (cookie.getPath() != null) {
            cookieHeader.append("; Path=").append(cookie.getPath());
        }

        if (cookie.getDomain() != null) {
            cookieHeader.append("; Domain=").append(cookie.getDomain());
        }

        if (cookie.getMaxAge() >= 0) {
            cookieHeader.append("; Max-Age=").append(cookie.getMaxAge());
        }

        if (cookie.getSecure()) {
            cookieHeader.append("; Secure");
        }

        if (cookie.isHttpOnly()) {
            cookieHeader.append("; HttpOnly");
        }

        addHeader("Set-Cookie", cookieHeader.toString());
    }
}
