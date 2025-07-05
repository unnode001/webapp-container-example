package com.webcontainer.stage5;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webcontainer.stage3.ServletContainer;
import com.webcontainer.stage4.SessionManager;

/**
 * 第五阶段：Context容器
 * 代表一个独立的Web应用，包含独立的类加载器、Servlet容器和配置
 */
public class WebAppContext implements ServletContext {

    private final String contextPath;
    private final String webAppName;
    private final File webAppDir;
    private final WebAppClassLoader classLoader;
    private final ServletContainer servletContainer;
    private final SessionManager sessionManager;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    private final Map<String, String> initParameters = new ConcurrentHashMap<>();

    private boolean started = false;

    public WebAppContext(String contextPath, String webAppName, File webAppDir, ClassLoader parentClassLoader) {
        this.contextPath = contextPath;
        this.webAppName = webAppName;
        this.webAppDir = webAppDir;

        // 创建独立的类加载器
        this.classLoader = new WebAppClassLoader(webAppName, webAppDir, parentClassLoader);

        // 创建独立的Servlet容器
        this.servletContainer = new ServletContainer();
        this.servletContainer.setServletContext(this);

        // 创建独立的会话管理器
        this.sessionManager = new SessionManager(this);

        System.out.println("创建Web应用Context: " + contextPath + " (" + webAppName + ")");
    }

    /**
     * 启动Web应用
     */
    public void start() throws Exception {
        if (started) {
            return;
        }

        System.out.println("启动Web应用: " + contextPath);

        // 在应用的类加载器上下文中初始化
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        try {
            currentThread.setContextClassLoader(classLoader);

            // 加载web.xml配置（简化实现，这里手动配置一些示例Servlet）
            loadConfiguration();

            // Servlet容器已经在loadConfiguration中完成初始化

            started = true;
            System.out.println("Web应用启动成功: " + contextPath);

        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * 停止Web应用
     */
    public void stop() {
        if (!started) {
            return;
        }

        System.out.println("停止Web应用: " + contextPath);

        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        try {
            currentThread.setContextClassLoader(classLoader);

            // 销毁Servlet容器
            servletContainer.destroy();

            // 销毁会话管理器
            sessionManager.destroy();

            // 清理属性
            attributes.clear();

            started = false;
            System.out.println("Web应用停止成功: " + contextPath);

        } finally {
            currentThread.setContextClassLoader(originalClassLoader);

            // 销毁类加载器
            classLoader.destroy();
        }
    }

    /**
     * 处理HTTP请求
     */
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!started) {
            return false;
        }

        // 检查请求是否属于这个Context
        String requestPath = request.getRequestURI();
        if (!requestPath.startsWith(contextPath)) {
            return false;
        }

        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();

        try {
            // 设置应用的类加载器
            currentThread.setContextClassLoader(classLoader);

            // 调整请求路径（移除context path）
            String servletPath = requestPath.substring(contextPath.length());
            if (servletPath.isEmpty()) {
                servletPath = "/";
            }

            // 创建包装请求，设置正确的servlet path
            ContextAwareRequest contextRequest = new ContextAwareRequest(request, this, servletPath);

            // 委托给Servlet容器处理
            return servletContainer.handleRequest(contextRequest, response);

        } catch (Exception e) {
            System.err.println("处理请求时发生错误 [" + contextPath + "]: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * 加载Web应用配置（简化实现）
     */
    private void loadConfiguration() {
        // 这里可以解析web.xml文件，现在简化为手动配置
        System.out.println("加载Web应用配置: " + webAppName);

        // 为ROOT应用配置一些默认Servlet
        if ("ROOT".equals(webAppName)) {
            // 配置一些示例Servlet
            try {
                // 注册第五阶段的Context演示Servlet
                servletContainer.registerServlet("contextServlet",
                        "com.webcontainer.stage5.ContextDemoServlet", "/context");
                System.out.println("注册Context演示Servlet: /context");

                // 注册部署统计API
                servletContainer.registerServlet("deploymentStatsServlet",
                        "com.webcontainer.stage5.DeploymentStatsServlet", "/api/deployment");
                System.out.println("注册部署统计API: /api/deployment");

                // 注册第四阶段的Servlet（向后兼容）
                servletContainer.registerServlet("standardServlet",
                        "com.webcontainer.stage3.StandardServlet", "/servlet");
                System.out.println("注册标准Servlet: /servlet");

                servletContainer.registerServlet("apiServlet",
                        "com.webcontainer.stage3.ApiServlet", "/api/*");
                System.out.println("注册API Servlet: /api/*");

                servletContainer.registerServlet("sessionDemoServlet",
                        "com.webcontainer.stage4.SessionDemoServlet", "/session");
                System.out.println("注册会话演示Servlet: /session");

                servletContainer.registerServlet("sessionStatsServlet",
                        "com.webcontainer.stage4.SessionStatsServlet", "/api/sessions");
                System.out.println("注册会话统计API: /api/sessions");

            } catch (Exception e) {
                System.err.println("注册Servlet失败: " + e.getMessage());
            }
        }
    }

    // ServletContext接口实现

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        // 简化实现，返回自己
        return this;
    }

    @Override
    public int getMajorVersion() {
        return 4;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        // 简化实现
        if (file.endsWith(".html"))
            return "text/html";
        if (file.endsWith(".css"))
            return "text/css";
        if (file.endsWith(".js"))
            return "application/javascript";
        if (file.endsWith(".json"))
            return "application/json";
        return "application/octet-stream";
    }

    @Override
    public java.util.Set<String> getResourcePaths(String path) {
        // 简化实现
        return Collections.emptySet();
    }

    @Override
    public java.net.URL getResource(String path) {
        try {
            File resourceFile = new File(webAppDir, path);
            if (resourceFile.exists()) {
                return resourceFile.toURI().toURL();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }

    @Override
    public java.io.InputStream getResourceAsStream(String path) {
        try {
            java.net.URL url = getResource(path);
            if (url != null) {
                return url.openStream();
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return null;
    }

    @Override
    public javax.servlet.RequestDispatcher getRequestDispatcher(String path) {
        // 简化实现
        return null;
    }

    @Override
    public javax.servlet.RequestDispatcher getNamedDispatcher(String name) {
        // 简化实现
        return null;
    }

    @Override
    public void log(String msg) {
        System.out.println("[" + contextPath + "] " + msg);
    }

    @Override
    public void log(String message, Throwable throwable) {
        System.err.println("[" + contextPath + "] " + message);
        if (throwable != null) {
            throwable.printStackTrace();
        }
    }

    @Override
    public String getRealPath(String path) {
        try {
            return new File(webAppDir, path).getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getServerInfo() {
        return "轻量级Web容器/1.0.0-stage5";
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (initParameters.containsKey(name)) {
            return false;
        }
        initParameters.put(name, value);
        return true;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return webAppName;
    }

    // Servlet 4.0 新增方法的空实现
    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return null;
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName, javax.servlet.Servlet servlet) {
        return null;
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addServlet(String servletName,
            Class<? extends javax.servlet.Servlet> servletClass) {
        return null;
    }

    @Override
    public <T extends javax.servlet.Servlet> T createServlet(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public javax.servlet.ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public java.util.Map<String, ? extends javax.servlet.ServletRegistration> getServletRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return null;
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, javax.servlet.Filter filter) {
        return null;
    }

    @Override
    public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName,
            Class<? extends javax.servlet.Filter> filterClass) {
        return null;
    }

    @Override
    public <T extends javax.servlet.Filter> T createFilter(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public javax.servlet.FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public java.util.Map<String, ? extends javax.servlet.FilterRegistration> getFilterRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public javax.servlet.SessionCookieConfig getSessionCookieConfig() {
        return null;
    }

    @Override
    public void setSessionTrackingModes(java.util.Set<javax.servlet.SessionTrackingMode> sessionTrackingModes) {
    }

    @Override
    public java.util.Set<javax.servlet.SessionTrackingMode> getDefaultSessionTrackingModes() {
        return Collections.emptySet();
    }

    @Override
    public java.util.Set<javax.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return Collections.emptySet();
    }

    @Override
    public void addListener(String className) {
    }

    @Override
    public <T extends java.util.EventListener> void addListener(T t) {
    }

    @Override
    public void addListener(Class<? extends java.util.EventListener> listenerClass) {
    }

    @Override
    public <T extends java.util.EventListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public javax.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public void declareRoles(String... roleNames) {
    }

    @Override
    public String getVirtualServerName() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 30; // 30分钟
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
    }

    @Override
    public String getRequestCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
    }

    @Override
    public String getResponseCharacterEncoding() {
        return "UTF-8";
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
    }

    // Getter方法
    public String getWebAppName() {
        return webAppName;
    }

    public File getWebAppDir() {
        return webAppDir;
    }

    public WebAppClassLoader getWebAppClassLoader() {
        return classLoader;
    }

    public ServletContainer getServletContainer() {
        return servletContainer;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public boolean isStarted() {
        return started;
    }

    // ServletContext接口的额外必需方法

    @Override
    public int getEffectiveMajorVersion() {
        return 4;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    @Deprecated
    public javax.servlet.Servlet getServlet(String name) throws ServletException {
        return null;
    }

    @Override
    @Deprecated
    public java.util.Enumeration<javax.servlet.Servlet> getServlets() {
        return Collections.emptyEnumeration();
    }

    @Override
    @Deprecated
    public java.util.Enumeration<String> getServletNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    @Deprecated
    public void log(Exception exception, String msg) {
        log(msg, exception);
    }

    @Override
    public javax.servlet.ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        return null;
    }
}
