package com.webcontainer;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求封装类
 * 用于存储解析后的HTTP请求信息
 */
public class HttpRequest {
    private final String method;
    private final String uri;
    private final String protocol;
    private final Map<String, String> headers;

    public HttpRequest(String method, String uri, String protocol) {
        this.method = method;
        this.uri = uri;
        this.protocol = protocol;
        this.headers = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public void addHeader(String name, String value) {
        headers.put(name.toLowerCase(), value);
    }

    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", method, uri, protocol);
    }
}
