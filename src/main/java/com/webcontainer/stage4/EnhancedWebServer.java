package com.webcontainer.stage4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.webcontainer.stage2.StaticResourceHandler;
import com.webcontainer.stage3.ServletContainer;

/**
 * 第四阶段：增强Web服务器
 * 支持会话管理和线程池优化
 */
public class EnhancedWebServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "webapps/ROOT";

    // 线程池配置
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final int QUEUE_CAPACITY = 100;

    private ServerSocket serverSocket;
    private boolean running = false;
    private StaticResourceHandler staticHandler;
    private ServletContainer servletContainer;
    private SessionManager sessionManager;
    private ThreadPoolRequestProcessor requestProcessor;

    public EnhancedWebServer() {
        this.staticHandler = new StaticResourceHandler(WEB_ROOT);
        this.sessionManager = new SessionManager(null); // 简化实现，ServletContext暂为null
        this.servletContainer = new ServletContainer();
        this.requestProcessor = new ThreadPoolRequestProcessor(CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);

        // 初始化Servlet映射
        initServletMappings();
    }

    /**
     * 初始化Servlet映射关系
     */
    private void initServletMappings() {
        // 注册第三阶段的Servlet
        servletContainer.registerServlet("standardServlet",
                "com.webcontainer.stage3.StandardServlet", "/servlet");

        servletContainer.registerServlet("apiServlet",
                "com.webcontainer.stage3.ApiServlet", "/api/*");

        // 注册第四阶段的会话演示Servlet
        servletContainer.registerServlet("sessionDemoServlet",
                "com.webcontainer.stage4.SessionDemoServlet", "/session");

        // 注册会话统计API
        servletContainer.registerServlet("sessionStatsServlet",
                "com.webcontainer.stage4.SessionStatsServlet", "/api/sessions");

        System.out.println("Servlet映射初始化完成:");
        servletContainer.getUrlMappings()
                .forEach((pattern, servletName) -> System.out.println("  " + pattern + " -> " + servletName));
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;

        System.out.println("=================================");
        System.out.println("第四阶段增强Web服务器启动成功！");
        System.out.println("监听端口: " + PORT);
        System.out.println("静态资源目录: " + WEB_ROOT);
        System.out.println("=================================");
        System.out.println("新功能:");
        System.out.println("  ✅ HttpSession会话管理");
        System.out.println("  ✅ Cookie支持");
        System.out.println("  ✅ 线程池优化 (核心:" + CORE_POOL_SIZE + ", 最大:" + MAX_POOL_SIZE + ")");
        System.out.println("  ✅ 会话自动过期清理");
        System.out.println("=================================");
        System.out.println("访问示例:");
        System.out.println("  静态页面: http://localhost:" + PORT + "/");
        System.out.println("  标准Servlet: http://localhost:" + PORT + "/servlet");
        System.out.println("  会话演示: http://localhost:" + PORT + "/session");
        System.out.println("  API接口: http://localhost:" + PORT + "/api");
        System.out.println("  会话统计: http://localhost:" + PORT + "/api/sessions");
        System.out.println("=================================");

        // 主线程循环接收连接
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // 使用线程池处理请求
                requestProcessor.submit(new EnhancedRequestHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("接收连接时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;

        // 关闭线程池
        requestProcessor.shutdown();

        // 销毁会话管理器
        sessionManager.destroy();

        // 销毁Servlet容器
        servletContainer.destroy();

        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("第四阶段增强Web服务器已停止");
    }

    /**
     * 增强的请求处理器
     */
    private class EnhancedRequestHandler implements Runnable {
        private final Socket clientSocket;

        public EnhancedRequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    OutputStream outputStream = clientSocket.getOutputStream()) {

                // 解析HTTP请求
                EnhancedHttpServletRequestImpl request = parseRequest(reader);
                EnhancedHttpServletResponseImpl response = new EnhancedHttpServletResponseImpl();

                // 首先尝试Servlet处理
                boolean handledByServlet = servletContainer.handleRequest(request, response);

                if (!handledByServlet) {
                    // 如果没有Servlet处理，则作为静态资源处理
                    handleStaticRequest(request, response);
                }

                // 发送响应
                sendResponse(outputStream, response);

            } catch (Exception e) {
                System.err.println("处理请求时发生错误: " + e.getMessage());
                e.printStackTrace();
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
        private EnhancedHttpServletRequestImpl parseRequest(BufferedReader reader) throws IOException {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                throw new IOException("无效的HTTP请求");
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                throw new IOException("无效的请求行格式: " + requestLine);
            }

            EnhancedHttpServletResponseImpl response = new EnhancedHttpServletResponseImpl();
            EnhancedHttpServletRequestImpl request = new EnhancedHttpServletRequestImpl(
                    requestParts[0], requestParts[1], requestParts[2], sessionManager, response);

            String headerLine;
            while ((headerLine = reader.readLine()) != null && !headerLine.trim().isEmpty()) {
                int colonIndex = headerLine.indexOf(':');
                if (colonIndex > 0) {
                    String headerName = headerLine.substring(0, colonIndex).trim();
                    String headerValue = headerLine.substring(colonIndex + 1).trim();
                    request.addHeader(headerName, headerValue);
                }
            }

            return request;
        }

        /**
         * 处理静态资源请求
         */
        private void handleStaticRequest(EnhancedHttpServletRequestImpl request,
                EnhancedHttpServletResponseImpl response) {
            StaticResourceHandler.StaticResource resource = staticHandler.handleRequest(request.getRequestURI());

            response.setStatus(resource.getStatusCode());
            response.setHeader("Content-Type", resource.getMimeType());
            response.setHeader("Content-Length", String.valueOf(resource.getContentLength()));

            try {
                response.getWriter().write(new String(resource.getContent()));
            } catch (IOException e) {
                System.err.println("写入静态资源响应时发生错误: " + e.getMessage());
            }

            System.out.println("静态资源处理: " + request.getRequestURI() + " -> " +
                    resource.getStatusCode() + " " + resource.getReasonPhrase());
        }

        /**
         * 发送HTTP响应
         */
        private void sendResponse(OutputStream outputStream, EnhancedHttpServletResponseImpl response)
                throws IOException {
            PrintWriter writer = new PrintWriter(outputStream);

            // 发送状态行
            writer.println("HTTP/1.1 " + response.getStatus() + " " + response.getReasonPhrase());

            // 发送响应头
            for (String headerName : response.getHeaderNames()) {
                writer.println(headerName + ": " + response.getHeader(headerName));
            }

            // 发送空行
            writer.println();

            // 发送响应体
            String content = response.getContent();
            if (content != null && !content.isEmpty()) {
                writer.print(content);
            }

            writer.flush();
        }
    }

    public static void main(String[] args) {
        EnhancedWebServer server = new EnhancedWebServer();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (IOException e) {
                System.err.println("停止服务器时发生错误: " + e.getMessage());
            }
        }));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("启动服务器失败: " + e.getMessage());
        }
    }
}
