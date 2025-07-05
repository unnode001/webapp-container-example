package com.webcontainer.stage3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * 第三阶段：实现标准HttpServletRequest接口
 * 包装HTTP请求信息，符合Servlet API规范
 */
public class HttpServletRequestImpl implements HttpServletRequest {
    private final String method;
    private final String requestURI;
    private final String protocol;
    private final Map<String, String> headers;
    private final Map<String, String[]> parameters;
    private final String queryString;
    private final String contextPath;
    private final String servletPath;
    private final String pathInfo;

    public HttpServletRequestImpl(String method, String requestURI, String protocol) {
        this.method = method;
        this.protocol = protocol;
        this.headers = new HashMap<>();
        this.parameters = new HashMap<>();

        // 解析URI和查询字符串
        int queryIndex = requestURI.indexOf('?');
        if (queryIndex != -1) {
            this.requestURI = requestURI.substring(0, queryIndex);
            this.queryString = requestURI.substring(queryIndex + 1);
            parseQueryString(this.queryString);
        } else {
            this.requestURI = requestURI;
            this.queryString = null;
        }

        // 设置上下文路径和Servlet路径（简化实现）
        this.contextPath = "";
        this.servletPath = this.requestURI;
        this.pathInfo = null;
    }

    /**
     * 解析查询字符串中的参数
     */
    private void parseQueryString(String queryString) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }

        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            try {
                String key = idx > 0 ? java.net.URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                String value = idx > 0 && pair.length() > idx + 1
                        ? java.net.URLDecoder.decode(pair.substring(idx + 1), "UTF-8")
                        : "";

                String[] existingValues = parameters.get(key);
                if (existingValues == null) {
                    parameters.put(key, new String[] { value });
                } else {
                    String[] newValues = Arrays.copyOf(existingValues, existingValues.length + 1);
                    newValues[existingValues.length] = value;
                    parameters.put(key, newValues);
                }
            } catch (UnsupportedEncodingException e) {
                // UTF-8 应该始终支持
                System.err.println("URL解码错误: " + e.getMessage());
            }
        }
    }

    public void addHeader(String name, String value) {
        headers.put(name.toLowerCase(), value);
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = getHeader(name);
        if (value == null) {
            return Collections.emptyEnumeration();
        }
        return Collections.enumeration(Arrays.asList(value));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headers.keySet());
    }

    @Override
    public String getParameter(String name) {
        String[] values = parameters.get(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return new HashMap<>(parameters);
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer("http://localhost:8080" + requestURI);
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public String getServerName() {
        return "localhost";
    }

    @Override
    public int getServerPort() {
        return 8080;
    }

    // 简化实现的方法
    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public String getCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
    }

    @Override
    public int getContentLength() {
        return -1;
    }

    @Override
    public long getContentLengthLong() {
        return -1L;
    }

    @Override
    public String getContentType() {
        return getHeader("content-type");
    }

    @Override
    public javax.servlet.ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException("InputStream not supported in this implementation");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException("Reader not supported in this implementation");
    }

    @Override
    public String getRemoteAddr() {
        return "127.0.0.1";
    }

    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    @Override
    public void setAttribute(String name, Object o) {
    }

    @Override
    public void removeAttribute(String name) {
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(Arrays.asList(Locale.getDefault()));
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public javax.servlet.RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return "localhost";
    }

    @Override
    public String getLocalAddr() {
        return "127.0.0.1";
    }

    @Override
    public int getLocalPort() {
        return 8080;
    }

    @Override
    public javax.servlet.ServletContext getServletContext() {
        return null;
    }

    @Override
    public javax.servlet.AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException("Async not supported");
    }

    @Override
    public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest servletRequest,
            javax.servlet.ServletResponse servletResponse)
            throws IllegalStateException {
        throw new UnsupportedOperationException("Async not supported");
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public javax.servlet.AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException("Async not supported");
    }

    @Override
    public javax.servlet.DispatcherType getDispatcherType() {
        return javax.servlet.DispatcherType.REQUEST;
    }

    // Servlet API 3.1+ 方法
    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException("Session management not implemented");
    }

    @Override
    public boolean authenticate(javax.servlet.http.HttpServletResponse response)
            throws IOException, javax.servlet.ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws javax.servlet.ServletException {
    }

    @Override
    public void logout() throws javax.servlet.ServletException {
    }

    @Override
    public java.util.Collection<javax.servlet.http.Part> getParts() throws IOException, javax.servlet.ServletException {
        return Collections.emptyList();
    }

    @Override
    public javax.servlet.http.Part getPart(String name) throws IOException, javax.servlet.ServletException {
        return null;
    }

    @Override
    public <T extends javax.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
            throws IOException, javax.servlet.ServletException {
        throw new UnsupportedOperationException("Upgrade not supported");
    }

    // Session 相关（简化实现）
    @Override
    public javax.servlet.http.HttpSession getSession(boolean create) {
        return null; // 第四阶段将实现
    }

    @Override
    public javax.servlet.http.HttpSession getSession() {
        return getSession(true);
    }

    // Cookie 相关
    @Override
    public javax.servlet.http.Cookie[] getCookies() {
        return new javax.servlet.http.Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if (value == null) {
            return -1L;
        }
        // 简化实现，实际应该解析日期格式
        return -1L;
    }

    @Override
    public int getIntHeader(String name) {
        String value = getHeader(name);
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String getPathTranslated() {
        return null;
    }
}
