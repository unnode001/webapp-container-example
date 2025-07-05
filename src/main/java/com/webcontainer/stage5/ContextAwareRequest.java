package com.webcontainer.stage5;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Context感知的请求包装器
 * 为Web应用请求提供正确的上下文信息
 */
public class ContextAwareRequest extends HttpServletRequestWrapper {

    private final WebAppContext webAppContext;
    private final String servletPath;

    public ContextAwareRequest(HttpServletRequest request, WebAppContext webAppContext, String servletPath) {
        super(request);
        this.webAppContext = webAppContext;
        this.servletPath = servletPath;
    }

    @Override
    public String getContextPath() {
        return webAppContext.getContextPath();
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public ServletContext getServletContext() {
        return webAppContext;
    }

    @Override
    public String getPathInfo() {
        String requestURI = getRequestURI();
        String contextPath = getContextPath();
        String servletPath = getServletPath();

        // 计算pathInfo = requestURI - contextPath - servletPath
        String fullServletPath = contextPath + servletPath;
        if (requestURI.length() > fullServletPath.length()) {
            return requestURI.substring(fullServletPath.length());
        }
        return null;
    }

    @Override
    public String getRequestURI() {
        return super.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return super.getRequestURL();
    }
}
