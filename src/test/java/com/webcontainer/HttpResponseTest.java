package com.webcontainer;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * HTTP响应类测试
 */
public class HttpResponseTest {

    @Test
    public void testHttpResponseCreation() {
        HttpResponse response = new HttpResponse();

        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getReasonPhrase());
        assertEquals("HTTP/1.1 200 OK", response.getStatusLine());
    }

    @Test
    public void testSetStatus() {
        HttpResponse response = new HttpResponse();
        response.setStatus(404, "Not Found");

        assertEquals(404, response.getStatusCode());
        assertEquals("Not Found", response.getReasonPhrase());
        assertEquals("HTTP/1.1 404 Not Found", response.getStatusLine());
    }

    @Test
    public void testAddHeader() {
        HttpResponse response = new HttpResponse();
        response.addHeader("Content-Type", "text/html");
        response.addHeader("Content-Length", "100");

        assertEquals(2, response.getHeaders().size());
        assertTrue(response.getHeaders().contains("Content-Type: text/html"));
        assertTrue(response.getHeaders().contains("Content-Length: 100"));
    }

    @Test
    public void testSetBody() {
        HttpResponse response = new HttpResponse();
        String body = "<html><body>Test</body></html>";
        response.setBody(body);

        assertEquals(body, response.getBody());
    }
}
