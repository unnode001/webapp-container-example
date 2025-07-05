package com.webcontainer.stage4;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * 第四阶段：HttpSession实现
 * 支持会话状态管理和属性存储
 */
@SuppressWarnings("deprecation")
public class HttpSessionImpl implements HttpSession {
    private final String sessionId;
    private final long creationTime;
    private final ConcurrentHashMap<String, Object> attributes;
    private long lastAccessedTime;
    private int maxInactiveInterval = 30 * 60; // 默认30分钟
    private boolean isNew = true;
    private boolean valid = true;
    private final ServletContext servletContext;

    public HttpSessionImpl(String sessionId, ServletContext servletContext) {
        this.sessionId = sessionId;
        this.servletContext = servletContext;
        this.creationTime = System.currentTimeMillis();
        this.lastAccessedTime = this.creationTime;
        this.attributes = new ConcurrentHashMap<>();

        System.out.println("创建新会话: " + sessionId + " 于 " +
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(creationTime), ZoneId.systemDefault()));
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public long getCreationTime() {
        checkValid();
        return creationTime;
    }

    @Override
    public long getLastAccessedTime() {
        checkValid();
        return lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value == null) {
            removeAttribute(name);
            return;
        }

        Object oldValue = attributes.put(name, value);
        System.out.println("会话 " + sessionId + " 设置属性: " + name + " = " + value);

        // 这里可以触发HttpSessionAttributeListener事件（简化版本暂不实现）
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        Object oldValue = attributes.remove(name);
        if (oldValue != null) {
            System.out.println("会话 " + sessionId + " 移除属性: " + name);
        }

        // 这里可以触发HttpSessionAttributeListener事件（简化版本暂不实现）
    }

    @Override
    public void invalidate() {
        checkValid();

        // 清除所有属性
        attributes.clear();
        valid = false;

        System.out.println("会话 " + sessionId + " 已失效");

        // 这里可以触发HttpSessionListener事件（简化版本暂不实现）
    }

    @Override
    public boolean isNew() {
        checkValid();
        return isNew;
    }

    /**
     * 访问会话时调用，更新最后访问时间
     */
    public void access() {
        checkValid();
        lastAccessedTime = System.currentTimeMillis();
        isNew = false;
    }

    /**
     * 检查会话是否过期
     */
    public boolean isExpired() {
        if (!valid) {
            return true;
        }

        if (maxInactiveInterval <= 0) {
            return false; // 永不过期
        }

        long now = System.currentTimeMillis();
        return (now - lastAccessedTime) > (maxInactiveInterval * 1000L);
    }

    /**
     * 检查会话是否有效
     */
    private void checkValid() {
        if (!valid) {
            throw new IllegalStateException("Session has been invalidated");
        }
        if (isExpired()) {
            invalidate();
            throw new IllegalStateException("Session has expired");
        }
    }

    /**
     * 获取会话信息摘要
     */
    public String getSessionInfo() {
        return String.format("Session[id=%s, created=%s, lastAccessed=%s, maxInactive=%ds, attributes=%d]",
                sessionId,
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(creationTime), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(lastAccessedTime), ZoneId.systemDefault()),
                maxInactiveInterval,
                attributes.size());
    }

    // 已弃用的方法
    @Override
    @Deprecated
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    @Deprecated
    public String[] getValueNames() {
        Enumeration<String> names = getAttributeNames();
        return Collections.list(names).toArray(new String[0]);
    }

    @Override
    @Deprecated
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    @Deprecated
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    @Deprecated
    public HttpSessionContext getSessionContext() {
        return null; // 已弃用，返回null
    }
}
