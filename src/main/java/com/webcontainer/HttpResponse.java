package com.webcontainer;

import java.util.ArrayList;
import java.util.List;

/**
 * HTTP响应封装类
 * 用于构建标准的HTTP响应
 */
public class HttpResponse {
    private int statusCode;
    private String reasonPhrase;
    private final List<String> headers;
    private String body;
    private byte[] binaryBody;

    public HttpResponse() {
        this.headers = new ArrayList<>();
        this.statusCode = 200;
        this.reasonPhrase = "OK";
    }

    public void setStatus(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public void addHeader(String name, String value) {
        headers.add(name + ": " + value);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setBinaryBody(byte[] binaryBody) {
        this.binaryBody = binaryBody;
        this.body = null; // 清除文本内容
    }

    public String getStatusLine() {
        return "HTTP/1.1 " + statusCode + " " + reasonPhrase;
    }

    public List<String> getHeaders() {
        return new ArrayList<>(headers);
    }

    public String getBody() {
        return body;
    }

    public byte[] getBinaryBody() {
        return binaryBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }
}
