package com.webcontainer.stage3;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * 第三阶段：实现标准HttpServletResponse接口
 * 包装HTTP响应信息，符合Servlet API规范
 */
public class HttpServletResponseImpl implements HttpServletResponse {
    private int statusCode = SC_OK;
    private String reasonPhrase = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private final List<javax.servlet.http.Cookie> cookies = new ArrayList<>();
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private String characterEncoding = "UTF-8";
    private String contentType;
    private boolean committed = false;

    public HttpServletResponseImpl() {
        this.stringWriter = new StringWriter();
        this.printWriter = new PrintWriter(stringWriter);
    }

    @Override
    public void setStatus(int sc) {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        this.statusCode = sc;
        this.reasonPhrase = getReasonPhrase(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        this.statusCode = sc;
        this.reasonPhrase = sm;
    }

    @Override
    public int getStatus() {
        return statusCode;
    }

    @Override
    public void setHeader(String name, String value) {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        headers.put(name, value);

        // 特殊处理Content-Type
        if ("Content-Type".equalsIgnoreCase(name)) {
            setContentType(value);
        }
    }

    @Override
    public void addHeader(String name, String value) {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }

        String existingValue = headers.get(name);
        if (existingValue == null) {
            headers.put(name, value);
        } else {
            headers.put(name, existingValue + "," + value);
        }

        if ("Content-Type".equalsIgnoreCase(name)) {
            setContentType(value);
        }
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        String value = headers.get(name);
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(","));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return new ArrayList<>(headers.keySet());
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsKey(name);
    }

    @Override
    public void setContentType(String type) {
        if (committed) {
            return;
        }

        this.contentType = type;

        // 解析字符编码
        if (type != null && type.contains("charset=")) {
            String[] parts = type.split("charset=");
            if (parts.length > 1) {
                String charset = parts[1].trim();
                if (charset.contains(";")) {
                    charset = charset.substring(0, charset.indexOf(";"));
                }
                this.characterEncoding = charset;
            }
        }

        headers.put("Content-Type", type);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (committed) {
            return;
        }
        this.characterEncoding = charset;
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public javax.servlet.ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("OutputStream not supported in this implementation");
    }

    @Override
    public void setContentLength(int len) {
        setIntHeader("Content-Length", len);
    }

    @Override
    public void setContentLengthLong(long len) {
        setHeader("Content-Length", String.valueOf(len));
    }

    @Override
    public void setIntHeader(String name, int value) {
        setHeader(name, String.valueOf(value));
    }

    @Override
    public void setDateHeader(String name, long date) {
        setHeader(name, new Date(date).toString());
    }

    @Override
    public void addIntHeader(String name, int value) {
        addHeader(name, String.valueOf(value));
    }

    @Override
    public void addDateHeader(String name, long date) {
        addHeader(name, new Date(date).toString());
    }

    @Override
    public void addCookie(javax.servlet.http.Cookie cookie) {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        cookies.add(cookie);
    }

    @Override
    public String encodeURL(String url) {
        return url; // 简化实现，不进行URL编码
    }

    @Override
    public String encodeRedirectURL(String url) {
        return url; // 简化实现
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }

        setStatus(sc);
        resetBuffer();

        String errorPage = createErrorPage(sc, msg);
        setContentType("text/html; charset=UTF-8");
        setContentLength(errorPage.getBytes(characterEncoding).length);

        printWriter.write(errorPage);
        printWriter.flush();
        committed = true;
    }

    @Override
    public void sendError(int sc) throws IOException {
        sendError(sc, getReasonPhrase(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }

        resetBuffer();
        setStatus(SC_MOVED_TEMPORARILY);
        setHeader("Location", location);
        committed = true;
    }

    @Override
    public void flushBuffer() throws IOException {
        printWriter.flush();
        committed = true;
    }

    @Override
    public void resetBuffer() {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    @Override
    public void reset() {
        if (committed) {
            throw new IllegalStateException("Response already committed");
        }

        statusCode = SC_OK;
        reasonPhrase = "OK";
        headers.clear();
        cookies.clear();
        resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return committed;
    }

    @Override
    public int getBufferSize() {
        return 8192; // 默认缓冲区大小
    }

    @Override
    public void setBufferSize(int size) {
        // 简化实现，不实际设置缓冲区大小
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public void setLocale(Locale loc) {
        // 简化实现
    }

    // 自定义方法：获取响应内容
    public String getContent() {
        printWriter.flush();
        return stringWriter.toString();
    }

    public List<javax.servlet.http.Cookie> getCookies() {
        return new ArrayList<>(cookies);
    }

    public Map<String, String> getHeaderMap() {
        return new HashMap<>(headers);
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    /**
     * 根据状态码获取默认的原因短语
     */
    private String getReasonPhrase(int statusCode) {
        switch (statusCode) {
            case 200:
                return "OK";
            case 201:
                return "Created";
            case 204:
                return "No Content";
            case 301:
                return "Moved Permanently";
            case 302:
                return "Found";
            case 304:
                return "Not Modified";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not Found";
            case 405:
                return "Method Not Allowed";
            case 500:
                return "Internal Server Error";
            case 501:
                return "Not Implemented";
            case 502:
                return "Bad Gateway";
            case 503:
                return "Service Unavailable";
            default:
                return "Unknown";
        }
    }

    /**
     * 创建错误页面HTML
     */
    private String createErrorPage(int statusCode, String message) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>" + statusCode + " " + getReasonPhrase(statusCode) + "</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; text-align: center; margin-top: 100px; }\n" +
                "        h1 { color: #e74c3c; }\n" +
                "        p { color: #7f8c8d; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>" + statusCode + " " + getReasonPhrase(statusCode) + "</h1>\n" +
                "    <p>" + (message != null ? message : "An error occurred") + "</p>\n" +
                "    <p><a href=\"/\">返回首页</a></p>\n" +
                "</body>\n" +
                "</html>";
    }
}
