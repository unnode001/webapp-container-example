package com.webcontainer.stage4;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 第四阶段：会话演示Servlet
 * 展示会话管理功能
 */
public class SessionDemoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();

        // 获取或创建会话
        HttpSession session = request.getSession();

        // 处理表单提交
        String action = request.getParameter("action");
        String key = request.getParameter("key");
        String value = request.getParameter("value");

        if ("set".equals(action) && key != null && value != null) {
            session.setAttribute(key, value);
        } else if ("remove".equals(action) && key != null) {
            session.removeAttribute(key);
        } else if ("invalidate".equals(action)) {
            session.invalidate();
            session = request.getSession(); // 创建新会话
        }

        // 更新访问计数
        Integer visitCount = (Integer) session.getAttribute("visitCount");
        if (visitCount == null) {
            visitCount = 1;
        } else {
            visitCount++;
        }
        session.setAttribute("visitCount", visitCount);

        // 生成HTML页面
        generatePage(out, session, request);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void generatePage(PrintWriter out, HttpSession session, HttpServletRequest request) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"zh-CN\">");
        out.println("<head>");
        out.println("    <meta charset=\"UTF-8\">");
        out.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("    <title>会话管理演示</title>");
        out.println("    <style>");
        out.println(
                "        body { font-family: 'Segoe UI', Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }");
        out.println("        .container { max-width: 1000px; margin: 0 auto; }");
        out.println(
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }");
        out.println(
                "        .card { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); margin-bottom: 20px; }");
        out.println(
                "        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }");
        out.println("        .form-group { margin: 15px 0; }");
        out.println(
                "        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #2c3e50; }");
        out.println(
                "        .form-group input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; }");
        out.println(
                "        .btn { display: inline-block; background: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px; border: none; cursor: pointer; }");
        out.println("        .btn:hover { background: #2980b9; }");
        out.println("        .btn-danger { background: #e74c3c; }");
        out.println("        .btn-danger:hover { background: #c0392b; }");
        out.println(
                "        .attribute-list { background: #f8f9fa; padding: 15px; border-radius: 5px; border-left: 4px solid #3498db; }");
        out.println(
                "        .attribute-item { margin: 10px 0; padding: 10px; background: white; border-radius: 3px; display: flex; justify-content: space-between; align-items: center; }");
        out.println(
                "        .session-info { background: #e8f5e8; padding: 15px; border-radius: 5px; border-left: 4px solid #28a745; }");
        out.println("        .highlight { background: #fff3cd; padding: 10px; border-radius: 5px; margin: 10px 0; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class=\"container\">");
        out.println("        <div class=\"header\">");
        out.println("            <h1>🔐 会话管理演示</h1>");
        out.println("            <p>第四阶段：HttpSession功能展示</p>");
        out.println("        </div>");

        // 会话信息卡片
        out.println("        <div class=\"card\">");
        out.println("            <h2>📊 会话信息</h2>");
        out.println("            <div class=\"session-info\">");
        out.println("                <p><strong>会话ID:</strong> " + session.getId() + "</p>");
        out.println("                <p><strong>创建时间:</strong> " + formatTime(session.getCreationTime()) + "</p>");
        out.println("                <p><strong>最后访问:</strong> " + formatTime(session.getLastAccessedTime()) + "</p>");
        out.println("                <p><strong>超时时间:</strong> " + session.getMaxInactiveInterval() + " 秒</p>");
        out.println("                <p><strong>是否新会话:</strong> " + (session.isNew() ? "是" : "否") + "</p>");
        out.println("                <p><strong>访问次数:</strong> " + session.getAttribute("visitCount") + "</p>");
        out.println("            </div>");
        out.println("        </div>");

        // 会话属性管理
        out.println("        <div class=\"info-grid\">");
        out.println("            <div class=\"card\">");
        out.println("                <h3>📝 会话属性管理</h3>");
        out.println("                <form method=\"POST\">");
        out.println("                    <input type=\"hidden\" name=\"action\" value=\"set\">");
        out.println("                    <div class=\"form-group\">");
        out.println("                        <label for=\"key\">属性名:</label>");
        out.println(
                "                        <input type=\"text\" id=\"key\" name=\"key\" placeholder=\"例如: username\" required>");
        out.println("                    </div>");
        out.println("                    <div class=\"form-group\">");
        out.println("                        <label for=\"value\">属性值:</label>");
        out.println(
                "                        <input type=\"text\" id=\"value\" name=\"value\" placeholder=\"例如: admin\" required>");
        out.println("                    </div>");
        out.println("                    <button type=\"submit\" class=\"btn\">设置属性</button>");
        out.println("                </form>");
        out.println("            </div>");

        // 当前属性列表
        out.println("            <div class=\"card\">");
        out.println("                <h3>📋 当前会话属性</h3>");
        out.println("                <div class=\"attribute-list\">");

        Enumeration<String> attributeNames = session.getAttributeNames();
        if (!attributeNames.hasMoreElements()) {
            out.println("                    <p>暂无会话属性</p>");
        } else {
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                Object value = session.getAttribute(name);
                out.println("                    <div class=\"attribute-item\">");
                out.println("                        <span><strong>" + escapeHtml(name) + ":</strong> "
                        + escapeHtml(String.valueOf(value)) + "</span>");
                out.println("                        <form method=\"POST\" style=\"display: inline;\">");
                out.println("                            <input type=\"hidden\" name=\"action\" value=\"remove\">");
                out.println("                            <input type=\"hidden\" name=\"key\" value=\""
                        + escapeHtml(name) + "\">");
                out.println("                            <button type=\"submit\" class=\"btn btn-danger\">删除</button>");
                out.println("                        </form>");
                out.println("                    </div>");
            }
        }

        out.println("                </div>");
        out.println("            </div>");
        out.println("        </div>");

        // 操作按钮
        out.println("        <div class=\"card\">");
        out.println("            <h3>🎮 会话操作</h3>");
        out.println("            <div style=\"text-align: center;\">");
        out.println("                <a href=\"" + request.getRequestURI() + "\" class=\"btn\">🔄 刷新页面</a>");
        out.println("                <form method=\"POST\" style=\"display: inline;\">");
        out.println("                    <input type=\"hidden\" name=\"action\" value=\"invalidate\">");
        out.println("                    <button type=\"submit\" class=\"btn btn-danger\">🗑️ 销毁会话</button>");
        out.println("                </form>");
        out.println("                <a href=\"/\" class=\"btn\">🏠 返回首页</a>");
        out.println("                <a href=\"/api/sessions\" class=\"btn\">📊 会话统计</a>");
        out.println("            </div>");
        out.println("        </div>");

        // 使用说明
        out.println("        <div class=\"card\">");
        out.println("            <h3>💡 使用说明</h3>");
        out.println("            <div class=\"highlight\">");
        out.println("                <p>• <strong>会话属性:</strong> 可以在会话中存储键值对数据，数据在整个会话期间保持</p>");
        out.println("                <p>• <strong>会话超时:</strong> 默认30分钟无活动后会话自动过期</p>");
        out.println("                <p>• <strong>会话ID:</strong> 通过Cookie (JSESSIONID) 在客户端和服务器之间传递</p>");
        out.println("                <p>• <strong>访问计数:</strong> 每次访问页面都会自动增加visitCount属性</p>");
        out.println("            </div>");
        out.println("        </div>");

        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }

    private String formatTime(long timestamp) {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String escapeHtml(String text) {
        if (text == null)
            return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("SessionDemoServlet 初始化完成");
    }

    @Override
    public void destroy() {
        super.destroy();
        System.out.println("SessionDemoServlet 销毁");
    }
}
