package com.webcontainer.stage3;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 第三阶段：API Servlet示例
 * 演示JSON API响应
 */
public class ApiServlet extends HttpServlet {
    private int requestCount = 0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        synchronized (this) {
            requestCount++;
        }

        // 设置响应为JSON格式
        response.setContentType("application/json; charset=UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*"); // 允许跨域

        PrintWriter out = response.getWriter();

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            pathInfo = "";
        }

        // 根据路径返回不同的API响应
        switch (pathInfo) {
            case "/status":
                handleStatusRequest(request, response, out);
                break;
            case "/time":
                handleTimeRequest(request, response, out);
                break;
            case "/echo":
                handleEchoRequest(request, response, out);
                break;
            default:
                handleDefaultRequest(request, response, out);
                break;
        }
    }

    /**
     * 处理状态查询请求
     */
    private void handleStatusRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        out.println("{");
        out.println("  \"status\": \"ok\",");
        out.println("  \"message\": \"Web容器运行正常\",");
        out.println("  \"server\": \"轻量级Web容器\",");
        out.println("  \"version\": \"1.0.0-stage3\",");
        out.println("  \"requestCount\": " + requestCount + ",");
        out.println("  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"");
        out.println("}");
    }

    /**
     * 处理时间查询请求
     */
    private void handleTimeRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        LocalDateTime now = LocalDateTime.now();
        out.println("{");
        out.println("  \"currentTime\": \"" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\",");
        out.println("  \"iso8601\": \"" + now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\",");
        out.println("  \"timestamp\": " + System.currentTimeMillis() + ",");
        out.println("  \"timezone\": \"" + java.time.ZoneId.systemDefault().toString() + "\"");
        out.println("}");
    }

    /**
     * 处理回声请求
     */
    private void handleEchoRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String message = request.getParameter("message");
        if (message == null) {
            message = "Hello, World!";
        }

        out.println("{");
        out.println("  \"echo\": \"" + escapeJson(message) + "\",");
        out.println("  \"method\": \"" + request.getMethod() + "\",");
        out.println("  \"uri\": \"" + request.getRequestURI() + "\",");
        out.println(
                "  \"queryString\": \"" + (request.getQueryString() != null ? request.getQueryString() : "") + "\",");
        out.println("  \"userAgent\": \"" + escapeJson(request.getHeader("User-Agent")) + "\",");
        out.println("  \"remoteAddr\": \"" + request.getRemoteAddr() + "\"");
        out.println("}");
    }

    /**
     * 处理默认API请求
     */
    private void handleDefaultRequest(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        out.println("{");
        out.println("  \"message\": \"欢迎使用轻量级Web容器API\",");
        out.println("  \"version\": \"1.0.0-stage4\",");
        out.println("  \"endpoints\": [");
        out.println("    {");
        out.println("      \"path\": \"/api/status\",");
        out.println("      \"description\": \"获取服务器状态信息\"");
        out.println("    },");
        out.println("    {");
        out.println("      \"path\": \"/api/time\",");
        out.println("      \"description\": \"获取当前服务器时间\"");
        out.println("    },");
        out.println("    {");
        out.println("      \"path\": \"/api/echo?message=your_message\",");
        out.println("      \"description\": \"回声测试，返回您发送的消息\"");
        out.println("    },");
        out.println("    {");
        out.println("      \"path\": \"/api/sessions\",");
        out.println("      \"description\": \"获取会话管理统计信息\"");
        out.println("    },");
        out.println("    {");
        out.println("      \"path\": \"/api/sessions/list\",");
        out.println("      \"description\": \"获取所有活跃会话列表\"");
        out.println("    },");
        out.println("    {");
        out.println("      \"path\": \"/api/sessions/cleanup\",");
        out.println("      \"description\": \"手动触发会话清理\"");
        out.println("    }");
        out.println("  ],");
        out.println("  \"servlets\": [");
        out.println("    {");
        out.println("      \"path\": \"/session\",");
        out.println("      \"description\": \"会话管理演示页面\"");
        out.println("    }");
        out.println("  ],");
        out.println("  \"requestCount\": " + requestCount + ",");
        out.println("  \"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "\"");
        out.println("}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST请求也使用相同的处理逻辑
        doGet(request, response);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 支持CORS预检请求
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * JSON字符串转义
     */
    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("ApiServlet 初始化完成");
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("ApiServlet 销毁，共处理了 " + requestCount + " 个API请求");
    }
}
