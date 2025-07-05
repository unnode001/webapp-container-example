package com.webcontainer.stage3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 第三阶段：Servlet容器
 * 负责Servlet的生命周期管理（加载、初始化、服务、销毁）
 */
public class ServletContainer {
    // Servlet实例缓存（单例模式）
    private final Map<String, Servlet> servletInstances = new ConcurrentHashMap<>();
    // Servlet配置信息
    private final Map<String, ServletConfig> servletConfigs = new HashMap<>();
    // URL模式到Servlet名称的映射
    private final Map<String, String> urlMappings = new HashMap<>();
    // Servlet名称到类名的映射
    private final Map<String, String> servletClasses = new HashMap<>();
    // ServletContext引用
    private javax.servlet.ServletContext servletContext;

    /**
     * 注册Servlet
     * 
     * @param servletName  Servlet名称
     * @param servletClass Servlet类名
     * @param urlPattern   URL模式
     */
    public void registerServlet(String servletName, String servletClass, String urlPattern) {
        servletClasses.put(servletName, servletClass);
        urlMappings.put(urlPattern, servletName);

        System.out.println("注册Servlet: " + servletName + " (" + servletClass + ") -> " + urlPattern);
    }

    /**
     * 处理HTTP请求
     * 
     * @param request  HTTP请求
     * @param response HTTP响应
     * @return true如果找到对应的Servlet并处理了请求，false否则
     */
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response) {
        String requestURI = request.getRequestURI();
        String servletName = findServletByURI(requestURI);

        if (servletName == null) {
            return false; // 没有找到对应的Servlet
        }

        try {
            Servlet servlet = getServletInstance(servletName);
            if (servlet instanceof HttpServlet) {
                HttpServlet httpServlet = (HttpServlet) servlet;
                httpServlet.service(request, response);
            } else {
                servlet.service(request, response);
            }

            System.out.println("Servlet处理完成: " + requestURI + " -> " + servletName);
            return true;

        } catch (Exception e) {
            System.err.println("Servlet处理错误: " + e.getMessage());
            e.printStackTrace();

            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Servlet processing error: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("发送错误响应失败: " + ex.getMessage());
            }
            return true; // 即使出错也算是处理了请求
        }
    }

    /**
     * 根据URI找到对应的Servlet名称
     */
    private String findServletByURI(String uri) {
        // 精确匹配
        String servletName = urlMappings.get(uri);
        if (servletName != null) {
            return servletName;
        }

        // 前缀匹配（例如 /api/* )
        for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.endsWith("/*")) {
                String prefix = pattern.substring(0, pattern.length() - 2);
                if (uri.startsWith(prefix)) {
                    return entry.getValue();
                }
            }
        }

        // 后缀匹配（例如 *.jsp）
        for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
            String pattern = entry.getKey();
            if (pattern.startsWith("*.")) {
                String suffix = pattern.substring(1);
                if (uri.endsWith(suffix)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 获取Servlet实例（单例模式）
     */
    private Servlet getServletInstance(String servletName) throws Exception {
        Servlet servlet = servletInstances.get(servletName);

        if (servlet == null) {
            synchronized (this) {
                servlet = servletInstances.get(servletName);
                if (servlet == null) {
                    servlet = createServletInstance(servletName);
                    servletInstances.put(servletName, servlet);
                }
            }
        }

        return servlet;
    }

    /**
     * 创建Servlet实例并初始化
     */
    private Servlet createServletInstance(String servletName) throws Exception {
        String className = servletClasses.get(servletName);
        if (className == null) {
            throw new ServletException("Servlet class not found: " + servletName);
        }

        // 使用反射创建实例
        Class<?> servletClass = Class.forName(className);
        Servlet servlet = (Servlet) servletClass.getDeclaredConstructor().newInstance();

        // 创建ServletConfig
        ServletConfig config = new SimpleServletConfig(servletName, servletContext);
        servletConfigs.put(servletName, config);

        // 初始化Servlet
        servlet.init(config);

        System.out.println("创建并初始化Servlet实例: " + servletName + " (" + className + ")");
        return servlet;
    }

    /**
     * 销毁所有Servlet实例
     */
    public void destroy() {
        for (Map.Entry<String, Servlet> entry : servletInstances.entrySet()) {
            try {
                entry.getValue().destroy();
                System.out.println("销毁Servlet: " + entry.getKey());
            } catch (Exception e) {
                System.err.println("销毁Servlet时发生错误: " + entry.getKey() + " - " + e.getMessage());
            }
        }
        servletInstances.clear();
        servletConfigs.clear();
    }

    /**
     * 获取所有已注册的URL映射
     */
    public Map<String, String> getUrlMappings() {
        return new HashMap<>(urlMappings);
    }

    /**
     * 设置ServletContext
     */
    public void setServletContext(javax.servlet.ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * 简化的ServletConfig实现
     */
    private static class SimpleServletConfig implements ServletConfig {
        private final String servletName;
        private final javax.servlet.ServletContext servletContext;

        public SimpleServletConfig(String servletName, javax.servlet.ServletContext servletContext) {
            this.servletName = servletName;
            this.servletContext = servletContext;
        }

        @Override
        public String getServletName() {
            return servletName;
        }

        @Override
        public javax.servlet.ServletContext getServletContext() {
            return servletContext;
        }

        @Override
        public String getInitParameter(String name) {
            return null; // 简化实现
        }

        @Override
        public java.util.Enumeration<String> getInitParameterNames() {
            return java.util.Collections.emptyEnumeration();
        }
    }
}
