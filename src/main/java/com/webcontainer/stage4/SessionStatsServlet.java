package com.webcontainer.stage4;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 会话统计Servlet
 * 提供会话管理的统计信息和监控API
 */
public class SessionStatsServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        // 从增强请求中获取SessionManager
        // 注意：这里需要通过一种方式获取SessionManager实例
        // 在实际实现中，可以通过ServletContext或依赖注入获取
        System.out.println("SessionStatsServlet 初始化完成");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        PrintWriter writer = response.getWriter();

        // 获取SessionManager实例（从增强请求中获取）
        SessionManager sessionManager = null;
        if (request instanceof EnhancedHttpServletRequestImpl) {
            sessionManager = ((EnhancedHttpServletRequestImpl) request).getSessionManager();
        }

        if (sessionManager == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("{\"error\": \"SessionManager not available\"}");
            return;
        }

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // 返回会话统计摘要
                handleSessionSummary(writer, sessionManager);
            } else if (pathInfo.equals("/list")) {
                // 返回所有活跃会话列表
                handleSessionList(writer, sessionManager);
            } else if (pathInfo.equals("/cleanup")) {
                // 手动触发会话清理
                handleSessionCleanup(writer, sessionManager);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                writer.println("{\"error\": \"Endpoint not found\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("{\"error\": \"" + e.getMessage() + "\"}");
            System.err.println("SessionStatsServlet处理请求时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理会话统计摘要请求
     */
    private void handleSessionSummary(PrintWriter writer, SessionManager sessionManager) {
        int totalSessions = sessionManager.getActiveSessions().size();
        long currentTime = System.currentTimeMillis();

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .append("\",\n");
        json.append("  \"activeSessions\": ").append(totalSessions).append(",\n");
        json.append("  \"sessionTimeout\": ").append(sessionManager.getSessionTimeout()).append(",\n");
        json.append("  \"serverUptime\": \"").append(formatUptime(currentTime)).append("\",\n");
        json.append("  \"endpoints\": {\n");
        json.append("    \"summary\": \"/api/sessions\",\n");
        json.append("    \"sessionList\": \"/api/sessions/list\",\n");
        json.append("    \"cleanup\": \"/api/sessions/cleanup\"\n");
        json.append("  }\n");
        json.append("}");

        writer.println(json.toString());

        System.out.println("会话统计摘要: 活跃会话数=" + totalSessions);
    }

    /**
     * 处理会话列表请求
     */
    private void handleSessionList(PrintWriter writer, SessionManager sessionManager) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .append("\",\n");
        json.append("  \"totalSessions\": ").append(sessionManager.getActiveSessions().size()).append(",\n");
        json.append("  \"sessions\": [\n");

        boolean first = true;
        for (HttpSessionImpl session : sessionManager.getActiveSessions().values()) {
            if (!first) {
                json.append(",\n");
            }
            first = false;

            json.append("    {\n");
            json.append("      \"sessionId\": \"").append(session.getId()).append("\",\n");
            json.append("      \"creationTime\": \"")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(session.getCreationTime())))
                    .append("\",\n");
            json.append("      \"lastAccessedTime\": \"")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(session.getLastAccessedTime())))
                    .append("\",\n");
            json.append("      \"maxInactiveInterval\": ").append(session.getMaxInactiveInterval()).append(",\n");
            json.append("      \"isNew\": ").append(session.isNew()).append(",\n");
            // 获取属性名称列表
            java.util.List<String> attributeNames = new java.util.ArrayList<>();
            java.util.Enumeration<String> attrEnum = session.getAttributeNames();
            while (attrEnum.hasMoreElements()) {
                attributeNames.add(attrEnum.nextElement());
            }

            json.append("      \"attributeCount\": ").append(attributeNames.size()).append(",\n");
            json.append("      \"attributes\": [");

            boolean firstAttr = true;
            for (String attrName : attributeNames) {
                if (!firstAttr) {
                    json.append(", ");
                }
                firstAttr = false;
                json.append("\"").append(attrName).append("\"");
            }

            json.append("]\n");
            json.append("    }");
        }

        json.append("\n  ]\n");
        json.append("}");

        writer.println(json.toString());

        System.out.println("会话列表查询: 返回" + sessionManager.getActiveSessions().size() + "个活跃会话");
    }

    /**
     * 处理会话清理请求
     */
    private void handleSessionCleanup(PrintWriter writer, SessionManager sessionManager) {
        int beforeCount = sessionManager.getActiveSessions().size();

        // 手动触发过期会话清理
        sessionManager.cleanupExpiredSessions();

        int afterCount = sessionManager.getActiveSessions().size();
        int cleanedCount = beforeCount - afterCount;

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                .append("\",\n");
        json.append("  \"message\": \"Session cleanup completed\",\n");
        json.append("  \"beforeCount\": ").append(beforeCount).append(",\n");
        json.append("  \"afterCount\": ").append(afterCount).append(",\n");
        json.append("  \"cleanedCount\": ").append(cleanedCount).append("\n");
        json.append("}");

        writer.println(json.toString());

        System.out.println("会话清理执行: 清理前=" + beforeCount + ", 清理后=" + afterCount + ", 已清理=" + cleanedCount);
    }

    /**
     * 格式化运行时间
     */
    private String formatUptime(long currentTime) {
        // 简化实现，显示当前时间
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(currentTime));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // POST请求可以用于触发特定的会话管理操作
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        PrintWriter writer = response.getWriter();

        if (pathInfo != null && pathInfo.equals("/invalidate")) {
            // 强制使当前会话无效
            try {
                request.getSession(false).invalidate();
                writer.println("{\"message\": \"Current session invalidated\"}");
                System.out.println("强制使会话无效: " + request.getSession(false).getId());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                writer.println("{\"error\": \"No active session to invalidate\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            writer.println("{\"error\": \"Method not supported for this endpoint\"}");
        }
    }

    @Override
    public void destroy() {
        System.out.println("SessionStatsServlet 销毁");
        super.destroy();
    }
}
