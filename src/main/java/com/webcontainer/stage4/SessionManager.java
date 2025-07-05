package com.webcontainer.stage4;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 第四阶段：会话管理器
 * 负责会话的创建、查找、过期清理等
 */
public class SessionManager {
    private static final String SESSION_COOKIE_NAME = "JSESSIONID";
    private static final int DEFAULT_SESSION_TIMEOUT = 30 * 60; // 30分钟

    private final ConcurrentHashMap<String, HttpSessionImpl> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ServletContext servletContext;

    public SessionManager(ServletContext servletContext) {
        this.servletContext = servletContext;

        // 启动会话清理任务，每分钟清理一次过期会话
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 60, 60, TimeUnit.SECONDS);

        System.out.println("会话管理器初始化完成，清理间隔: 60秒");
    }

    /**
     * 获取或创建会话
     */
    public HttpSession getSession(HttpServletRequest request, HttpServletResponse response, boolean create) {
        String sessionId = getSessionIdFromRequest(request);
        HttpSessionImpl session = null;

        if (sessionId != null) {
            session = sessions.get(sessionId);
            if (session != null) {
                if (session.isExpired()) {
                    // 会话已过期，移除并创建新的
                    sessions.remove(sessionId);
                    session.invalidate();
                    session = null;
                } else {
                    // 更新访问时间
                    session.access();
                }
            }
        }

        if (session == null && create) {
            // 创建新会话
            sessionId = generateSessionId();
            session = new HttpSessionImpl(sessionId, servletContext);
            session.setMaxInactiveInterval(DEFAULT_SESSION_TIMEOUT);
            sessions.put(sessionId, session);

            // 设置会话Cookie
            setSessionCookie(response, sessionId);

            System.out.println("创建新会话: " + sessionId + "，当前会话总数: " + sessions.size());
        }

        return session;
    }

    /**
     * 从请求中获取会话ID
     */
    private String getSessionIdFromRequest(HttpServletRequest request) {
        // 首先从Cookie中查找
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 如果Cookie中没有，尝试从URL参数中获取（jsessionid）
        String sessionId = request.getParameter("jsessionid");
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            return sessionId;
        }

        return null;
    }

    /**
     * 设置会话Cookie
     */
    private void setSessionCookie(HttpServletResponse response, String sessionId) {
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        sessionCookie.setPath("/");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setMaxAge(-1); // 浏览器关闭时删除

        response.addCookie(sessionCookie);
    }

    /**
     * 生成唯一的会话ID
     */
    private String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 清理过期会话
     */
    public void cleanupExpiredSessions() {
        int expiredCount = 0;
        for (String sessionId : sessions.keySet()) {
            HttpSessionImpl session = sessions.get(sessionId);
            if (session != null && session.isExpired()) {
                sessions.remove(sessionId);
                session.invalidate();
                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            System.out.println("清理了 " + expiredCount + " 个过期会话，当前活跃会话: " + sessions.size());
        }
    }

    /**
     * 获取会话统计信息
     */
    public SessionStats getSessionStats() {
        int totalSessions = sessions.size();
        int activeSessions = 0;
        int expiredSessions = 0;

        for (HttpSessionImpl session : sessions.values()) {
            if (session.isExpired()) {
                expiredSessions++;
            } else {
                activeSessions++;
            }
        }

        return new SessionStats(totalSessions, activeSessions, expiredSessions);
    }

    /**
     * 获取所有活跃会话的映射表（用于统计和监控）
     */
    public ConcurrentHashMap<String, HttpSessionImpl> getActiveSessions() {
        return sessions;
    }

    /**
     * 获取默认会话超时时间
     */
    public int getSessionTimeout() {
        return DEFAULT_SESSION_TIMEOUT;
    }

    /**
     * 销毁会话管理器
     */
    public void destroy() {
        // 停止清理任务
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        // 销毁所有会话
        for (HttpSessionImpl session : sessions.values()) {
            session.invalidate();
        }
        sessions.clear();

        System.out.println("会话管理器已销毁");
    }

    /**
     * 会话统计信息
     */
    public static class SessionStats {
        private final int totalSessions;
        private final int activeSessions;
        private final int expiredSessions;

        public SessionStats(int totalSessions, int activeSessions, int expiredSessions) {
            this.totalSessions = totalSessions;
            this.activeSessions = activeSessions;
            this.expiredSessions = expiredSessions;
        }

        public int getTotalSessions() {
            return totalSessions;
        }

        public int getActiveSessions() {
            return activeSessions;
        }

        public int getExpiredSessions() {
            return expiredSessions;
        }

        @Override
        public String toString() {
            return String.format("SessionStats{total=%d, active=%d, expired=%d}",
                    totalSessions, activeSessions, expiredSessions);
        }
    }
}
