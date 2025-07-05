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
 * 第三阶段：标准HttpServlet示例
 * 演示符合javax.servlet规范的Servlet实现
 */
public class StandardServlet extends HttpServlet {
    private int requestCount = 0;
    private LocalDateTime initTime;

    @Override
    public void init() throws ServletException {
        super.init();
        initTime = LocalDateTime.now();
        System.out.println(
                "StandardServlet 初始化完成，时间: " + initTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        synchronized (this) {
            requestCount++;
        }

        // 设置响应内容类型
        response.setContentType("text/html; charset=UTF-8");

        // 获取请求参数
        String name = request.getParameter("name");
        String message = request.getParameter("message");

        // 构建响应HTML
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"zh-CN\">");
        out.println("<head>");
        out.println("    <meta charset=\"UTF-8\">");
        out.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("    <title>标准Servlet演示</title>");
        out.println("    <style>");
        out.println("        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; }");
        out.println("        .container { max-width: 800px; margin: 0 auto; }");
        out.println(
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }");
        out.println(
                "        .content { background: #f8f9fa; padding: 30px; border-radius: 10px; margin-bottom: 20px; }");
        out.println(
                "        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 20px; margin: 20px 0; }");
        out.println(
                "        .info-card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println("        .info-card h3 { margin-top: 0; color: #2c3e50; }");
        out.println(
                "        .btn { display: inline-block; background: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px; }");
        out.println("        .btn:hover { background: #2980b9; }");
        out.println("        .form-group { margin: 15px 0; }");
        out.println("        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; }");
        out.println(
                "        .form-group input, .form-group textarea { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; }");
        out.println("        .highlight { background: #fff3cd; padding: 10px; border-radius: 5px; margin: 10px 0; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class=\"container\">");
        out.println("        <div class=\"header\">");
        out.println("            <h1>🎯 标准Servlet演示</h1>");
        out.println("            <p>第三阶段：实现javax.servlet.http.HttpServlet</p>");
        out.println("        </div>");

        // 显示问候信息
        if (name != null && !name.trim().isEmpty()) {
            out.println("        <div class=\"highlight\">");
            out.println("            <h2>👋 Hello, " + escapeHtml(name) + "!</h2>");
            if (message != null && !message.trim().isEmpty()) {
                out.println("            <p><strong>您的消息:</strong> " + escapeHtml(message) + "</p>");
            }
            out.println("        </div>");
        }

        out.println("        <div class=\"content\">");
        out.println("            <h2>🔧 Servlet信息</h2>");
        out.println("            <div class=\"info-grid\">");

        // Servlet信息卡片
        out.println("                <div class=\"info-card\">");
        out.println("                    <h3>📊 统计信息</h3>");
        out.println("                    <p><strong>初始化时间:</strong> "
                + initTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
        out.println("                    <p><strong>请求次数:</strong> " + requestCount + "</p>");
        out.println("                    <p><strong>当前时间:</strong> "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "</p>");
        out.println("                </div>");

        // 请求信息卡片
        out.println("                <div class=\"info-card\">");
        out.println("                    <h3>📋 请求信息</h3>");
        out.println("                    <p><strong>请求方法:</strong> " + request.getMethod() + "</p>");
        out.println("                    <p><strong>请求URI:</strong> " + request.getRequestURI() + "</p>");
        out.println("                    <p><strong>查询字符串:</strong> "
                + (request.getQueryString() != null ? request.getQueryString() : "无") + "</p>");
        out.println("                    <p><strong>协议:</strong> " + request.getProtocol() + "</p>");
        out.println("                </div>");

        // 头部信息卡片
        out.println("                <div class=\"info-card\">");
        out.println("                    <h3>📡 请求头</h3>");
        out.println("                    <p><strong>User-Agent:</strong> " + request.getHeader("User-Agent") + "</p>");
        out.println("                    <p><strong>Accept:</strong> " + request.getHeader("Accept") + "</p>");
        out.println("                    <p><strong>Host:</strong> " + request.getHeader("Host") + "</p>");
        out.println("                </div>");

        // 服务器信息卡片
        out.println("                <div class=\"info-card\">");
        out.println("                    <h3>🖥️ 服务器信息</h3>");
        out.println("                    <p><strong>服务器名:</strong> " + request.getServerName() + "</p>");
        out.println("                    <p><strong>服务器端口:</strong> " + request.getServerPort() + "</p>");
        out.println("                    <p><strong>远程地址:</strong> " + request.getRemoteAddr() + "</p>");
        out.println("                    <p><strong>Servlet名称:</strong> " + getServletName() + "</p>");
        out.println("                </div>");

        out.println("            </div>");
        out.println("        </div>");

        // 交互表单
        out.println("        <div class=\"content\">");
        out.println("            <h2>🎮 互动测试</h2>");
        out.println("            <form method=\"GET\">");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label for=\"name\">您的姓名:</label>");
        out.println("                    <input type=\"text\" id=\"name\" name=\"name\" value=\""
                + (name != null ? escapeHtml(name) : "") + "\" placeholder=\"请输入您的姓名\">");
        out.println("                </div>");
        out.println("                <div class=\"form-group\">");
        out.println("                    <label for=\"message\">留言:</label>");
        out.println("                    <textarea id=\"message\" name=\"message\" rows=\"3\" placeholder=\"请输入您的留言\">"
                + (message != null ? escapeHtml(message) : "") + "</textarea>");
        out.println("                </div>");
        out.println("                <button type=\"submit\" class=\"btn\">提交</button>");
        out.println("                <a href=\"" + request.getRequestURI() + "\" class=\"btn\">清除</a>");
        out.println("            </form>");
        out.println("        </div>");

        // 导航链接
        out.println("        <div style=\"text-align: center; margin-top: 30px;\">");
        out.println("            <a href=\"/\" class=\"btn\">← 返回首页</a>");
        out.println("            <a href=\"/api\" class=\"btn\">API测试</a>");
        out.println("            <a href=\"" + request.getRequestURI()
                + "?name=测试用户&message=这是一条测试消息\" class=\"btn\">示例请求</a>");
        out.println("        </div>");

        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST请求也转到GET处理
        doGet(request, response);
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("StandardServlet 销毁，共处理了 " + requestCount + " 个请求");
    }

    /**
     * HTML转义，防止XSS攻击
     */
    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
