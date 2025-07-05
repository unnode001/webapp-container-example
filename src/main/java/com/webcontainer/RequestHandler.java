package com.webcontainer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 请求处理器 - 处理每个客户端连接
 * 实现HTTP请求解析和响应构建
 */
public class RequestHandler implements Runnable {
    private final Socket clientSocket;

    public RequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream())) {

            // 解析HTTP请求
            HttpRequest request = parseRequest(reader);

            // 构建HTTP响应
            HttpResponse response = buildResponse(request);

            // 发送响应
            sendResponse(writer, response);

        } catch (IOException e) {
            System.err.println("处理请求时发生错误: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("关闭客户端连接时发生错误: " + e.getMessage());
            }
        }
    }

    /**
     * 解析HTTP请求
     */
    private HttpRequest parseRequest(BufferedReader reader) throws IOException {
        // 读取请求行 (Request Line)
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.trim().isEmpty()) {
            throw new IOException("无效的HTTP请求");
        }

        // 解析请求行: GET /path HTTP/1.1
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length != 3) {
            throw new IOException("无效的请求行格式: " + requestLine);
        }

        String method = requestParts[0];
        String uri = requestParts[1];
        String protocol = requestParts[2];

        HttpRequest request = new HttpRequest(method, uri, protocol);

        // 读取请求头
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.trim().isEmpty()) {
            int colonIndex = headerLine.indexOf(':');
            if (colonIndex > 0) {
                String headerName = headerLine.substring(0, colonIndex).trim();
                String headerValue = headerLine.substring(colonIndex + 1).trim();
                request.addHeader(headerName, headerValue);
            }
        }

        System.out.println("收到请求: " + method + " " + uri + " " + protocol);
        return request;
    }

    /**
     * 构建HTTP响应
     */
    private HttpResponse buildResponse(HttpRequest request) {
        // 第一阶段：返回硬编码的Hello World响应
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Simple Web Container</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; text-align: center; margin-top: 100px; }\n" +
                "        h1 { color: #2c3e50; }\n" +
                "        p { color: #7f8c8d; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Hello, World!</h1>\n" +
                "    <p>欢迎使用自制的轻量级Web容器</p>\n" +
                "    <p>请求路径: " + request.getUri() + "</p>\n" +
                "    <p>请求方法: " + request.getMethod() + "</p>\n" +
                "</body>\n" +
                "</html>";

        HttpResponse response = new HttpResponse();
        response.setStatus(200, "OK");
        response.addHeader("Content-Type", "text/html; charset=UTF-8");
        response.addHeader("Content-Length", String.valueOf(htmlContent.getBytes().length));
        response.addHeader("Connection", "close");
        response.setBody(htmlContent);

        return response;
    }

    /**
     * 发送HTTP响应
     */
    private void sendResponse(PrintWriter writer, HttpResponse response) {
        // 发送状态行
        writer.println(response.getStatusLine());

        // 发送响应头
        for (String header : response.getHeaders()) {
            writer.println(header);
        }

        // 发送空行（头部结束标志）
        writer.println();

        // 发送响应体
        if (response.getBody() != null) {
            writer.print(response.getBody());
        }

        writer.flush();
    }
}
