package com.webcontainer.stage5;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Context演示Servlet
 * 展示Web应用隔离和Context功能
 */
public class ContextDemoServlet extends HttpServlet {

    private int requestCount = 0;

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("ContextDemoServlet 初始化 - ClassLoader: " +
                Thread.currentThread().getContextClassLoader().getClass().getName());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        synchronized (this) {
            requestCount++;
        }

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang='zh-CN'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <title>Context演示 - 第五阶段</title>");
        out.println("    <style>");
        out.println("        body { font-family: Arial, sans-serif; margin: 40px; }");
        out.println("        .container { max-width: 800px; margin: 0 auto; }");
        out.println("        .info-box { background: #f5f5f5; padding: 15px; margin: 10px 0; border-radius: 5px; }");
        out.println("        .highlight { background: #e8f4fd; padding: 10px; border-left: 4px solid #2196F3; }");
        out.println("        .success { background: #e8f5e8; border-left: 4px solid #4CAF50; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("    <div class='container'>");
        out.println("        <h1>🚀 第五阶段：Web应用隔离演示</h1>");

        out.println("        <div class='success'>");
        out.println("            <h3>✅ Context隔离成功！</h3>");
        out.println("            <p>这个Servlet运行在独立的Web应用Context中</p>");
        out.println("        </div>");

        out.println("        <div class='info-box'>");
        out.println("            <h3>Context信息</h3>");
        out.println("            <p><strong>Context Path:</strong> " + request.getContextPath() + "</p>");
        out.println("            <p><strong>Servlet Path:</strong> " + request.getServletPath() + "</p>");
        out.println("            <p><strong>Path Info:</strong> " + request.getPathInfo() + "</p>");
        out.println("            <p><strong>Request URI:</strong> " + request.getRequestURI() + "</p>");
        out.println("            <p><strong>请求计数:</strong> " + requestCount + "</p>");
        out.println("        </div>");

        out.println("        <div class='info-box'>");
        out.println("            <h3>ClassLoader信息</h3>");
        ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
        out.println("            <p><strong>当前ClassLoader:</strong> " + currentCL.getClass().getName() + "</p>");
        if (currentCL instanceof WebAppClassLoader) {
            WebAppClassLoader webAppCL = (WebAppClassLoader) currentCL;
            out.println("            <p><strong>Web应用名:</strong> " + webAppCL.getWebAppName() + "</p>");
            out.println("            <p><strong>应用目录:</strong> " + webAppCL.getWebAppDir().getAbsolutePath() + "</p>");
        }
        out.println(
                "            <p><strong>父ClassLoader:</strong> " + currentCL.getParent().getClass().getName() + "</p>");
        out.println("        </div>");

        out.println("        <div class='info-box'>");
        out.println("            <h3>ServletContext信息</h3>");
        out.println(
                "            <p><strong>Context名称:</strong> " + getServletContext().getServletContextName() + "</p>");
        out.println("            <p><strong>服务器信息:</strong> " + getServletContext().getServerInfo() + "</p>");
        out.println("            <p><strong>Servlet版本:</strong> " + getServletContext().getMajorVersion() + "."
                + getServletContext().getMinorVersion() + "</p>");
        out.println("            <p><strong>Real Path:</strong> " + getServletContext().getRealPath("/") + "</p>");
        out.println("        </div>");

        out.println("        <div class='highlight'>");
        out.println("            <h3>🎯 第五阶段新特性</h3>");
        out.println("            <ul>");
        out.println("                <li>✅ 独立的Web应用ClassLoader</li>");
        out.println("                <li>✅ Context容器隔离</li>");
        out.println("                <li>✅ 独立的Servlet容器</li>");
        out.println("                <li>✅ 独立的会话管理</li>");
        out.println("                <li>🚧 动态部署（开发中）</li>");
        out.println("            </ul>");
        out.println("        </div>");

        out.println("        <div class='info-box'>");
        out.println("            <h3>测试链接</h3>");
        out.println("            <p><a href='/'>返回首页</a></p>");
        out.println("            <p><a href='/api'>API接口</a></p>");
        out.println("            <p><a href='/session'>会话演示</a></p>");
        out.println("            <p><a href='/api/sessions'>会话统计</a></p>");
        out.println("        </div>");

        out.println("    </div>");
        out.println("</body>");
        out.println("</html>");
    }

    @Override
    public void destroy() {
        System.out.println("ContextDemoServlet 销毁，共处理了 " + requestCount + " 个请求");
        super.destroy();
    }
}
