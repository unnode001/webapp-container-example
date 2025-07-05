package com.webcontainer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HTTP请求类测试
 */
public class HttpRequestTest {

    @Test
    public void testHttpRequestCreation() {
        HttpRequest request = new HttpRequest("GET", "/index.html", "HTTP/1.1");

        assertEquals("GET", request.getMethod());
        assertEquals("/index.html", request.getUri());
        assertEquals("HTTP/1.1", request.getProtocol());
    }

    @Test
    public void testAddHeader() {
        HttpRequest request = new HttpRequest("GET", "/", "HTTP/1.1");
        request.addHeader("Host", "localhost:8080");
        request.addHeader("User-Agent", "Test");

        assertEquals("localhost:8080", request.getHeader("Host"));
        assertEquals("localhost:8080", request.getHeader("host")); // 测试大小写不敏感
        assertEquals("Test", request.getHeader("User-Agent"));
        assertEquals(2, request.getHeaders().size());
    }

    @Test
    public void testToString() {
        HttpRequest request = new HttpRequest("POST", "/api/users", "HTTP/1.1");
        assertEquals("POST /api/users HTTP/1.1", request.toString());
    }
}
