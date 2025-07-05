package com.webcontainer.stage5;

import com.webcontainer.stage2.StaticResourceHandler;
import com.webcontainer.stage4.EnhancedHttpServletRequestImpl;
import com.webcontainer.stage4.EnhancedHttpServletResponseImpl;
import com.webcontainer.stage4.SessionManager;
import com.webcontainer.stage4.ThreadPoolRequestProcessor;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 第五阶段：多Web应用容器服务器
 * 支持Web应用隔离、动态部署和热加载
 */
public class MultiWebAppServer {
    private static final int PORT = 8080;
    private static final String WEB_APPS_DIR = "webapps";

    // 线程池配置
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 50;
    private static final int QUEUE_CAPACITY = 100;

    private ServerSocket serverSocket;
    private boolean running = false;
    private WebAppDeployer deployer;
    private ThreadPoolRequestProcessor requestProcessor;
    private SessionManager globalSessionManager; // 全局会话管理器，用于跨应用的会话处理

    public MultiWebAppServer() {
        // 初始化全局会话管理器
        this.globalSessionManager = new SessionManager(null);

        // 初始化部署器
        this.deployer = new WebAppDeployer(WEB_APPS_DIR, this.getClass().getClassLoader());

        // 初始化线程池
        this.requestProcessor = new ThreadPoolRequestProcessor(CORE_POOL_SIZE, MAX_POOL_SIZE, QUEUE_CAPACITY);

        System.out.println("多Web应用容器服务器初始化完成");
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;

        // 启动部署器
        deployer.start();

        System.out.println("=================================");
        System.out.println("第五阶段多Web应用容器服务器启动成功！");
        System.out.println("监听端口: " + PORT);
        System.out.println("Web应用目录: " + WEB_APPS_DIR);
        System.out.println("=================================");
        System.out.println("新功能:");
        System.out.println("  ✅ Web应用类加载器隔离");
        System.out.println("  ✅ 独立的Context容器");
        System.out.println("  ✅ 动态部署和热加载");
        System.out.println("  ✅ 多应用并存");
        System.out.println("  ✅ 线程池优化 (核心:" + CORE_POOL_SIZE + ", 最大:" + MAX_POOL_SIZE + ")");
        System.out.println("=================================");
        System.out.println("已部署的Web应用:");

        WebAppDeployer.DeploymentStats stats = deployer.getDeploymentStats();
        System.out.println("  总应用数: " + stats.getTotalApps());
        System.out.println("  运行中: " + stats.getRunningApps());

        for (WebAppContext context : deployer.getDeployedApps().values()) {
            String contextPath = context.getContextPath().isEmpty() ? "/" : context.getContextPath();
            System.out.println("  " + context.getWebAppName() + " -> " + contextPath +
                    " (状态: " + (context.isStarted() ? "运行中" : "已停止") + ")");
        }

        System.out.println("=================================");
        System.out.println("访问示例:");
        System.out.println("  ROOT应用: http://localhost:" + PORT + "/");
        System.out.println("  Context演示: http://localhost:" + PORT + "/context");
        System.out.println("  API接口: http://localhost:" + PORT + "/api");
        System.out.println("  会话演示: http://localhost:" + PORT + "/session");
        System.out.println("  部署统计: http://localhost:" + PORT + "/api/deployment");
        System.out.println("=================================");

        // 主线程循环接收连接
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // 使用线程池处理请求
                requestProcessor.submit(new MultiAppRequestHandler(clientSocket));
            } catch (IOException e) {
                if (running) {
                    System.err.println("接收连接时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;

        // 停止部署器
        deployer.stop();

        // 关闭线程池
        requestProcessor.shutdown();

        // 销毁全局会话管理器
        globalSessionManager.destroy();

        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("第五阶段多Web应用容器服务器已停止");
    }

    /**
     * 多应用请求处理器
     */
    private class MultiAppRequestHandler implements Runnable {
        private final Socket clientSocket;

        public MultiAppRequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    OutputStream outputStream = clientSocket.getOutputStream()) {

                // 解析HTTP请求
                EnhancedHttpServletRequestImpl request = parseRequest(reader);
                EnhancedHttpServletResponseImpl response = new EnhancedHttpServletResponseImpl();

                // 首先尝试通过部署器处理（查找匹配的Web应用）
                boolean handledByApp = deployer.handleRequest(request, response);

                if (!handledByApp) {
                    // 如果没有应用处理，则作为静态资源处理（从ROOT应用）
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
                    requestParts[0], requestParts[1], requestParts[2], globalSessionManager, response);

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
         * 处理静态资源请求（兜底处理）
         */
        private void handleStaticRequest(EnhancedHttpServletRequestImpl request,
                EnhancedHttpServletResponseImpl response) {
            // 从ROOT应用目录提供静态资源
            String rootPath = WEB_APPS_DIR + "/ROOT";
            StaticResourceHandler staticHandler = new StaticResourceHandler(rootPath);

            StaticResourceHandler.StaticResource resource = staticHandler.handleRequest(request.getRequestURI());

            response.setStatus(resource.getStatusCode());
            response.setHeader("Content-Type", resource.getMimeType());
            response.setHeader("Content-Length", String.valueOf(resource.getContentLength()));

            try {
                response.getWriter().write(new String(resource.getContent()));
            } catch (IOException e) {
                System.err.println("写入静态资源响应时发生错误: " + e.getMessage());
            }

            System.out.println("静态资源处理 [ROOT]: " + request.getRequestURI() + " -> " +
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
        MultiWebAppServer server = new MultiWebAppServer();

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
