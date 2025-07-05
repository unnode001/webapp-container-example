package com.webcontainer.stage3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.webcontainer.stage2.StaticResourceHandler;

/**
 * 第三阶段：标准Servlet API Web服务器
 * 支持标准javax.servlet规范
 */
public class StandardWebServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "webapps/ROOT";

    private ServerSocket serverSocket;
    private boolean running = false;
    private StaticResourceHandler staticHandler;
    private ServletContainer servletContainer;

    public StandardWebServer() {
        this.staticHandler = new StaticResourceHandler(WEB_ROOT);
        this.servletContainer = new ServletContainer();

        // 初始化Servlet映射
        initServletMappings();
    }

    /**
     * 初始化Servlet映射关系
     */
    private void initServletMappings() {
        // 注册标准Servlet
        servletContainer.registerServlet("standardServlet",
                "com.webcontainer.stage3.StandardServlet", "/servlet");

        // 注册API Servlet
        servletContainer.registerServlet("apiServlet",
                "com.webcontainer.stage3.ApiServlet", "/api/*");

        System.out.println("标准Servlet映射初始化完成:");
        servletContainer.getUrlMappings()
                .forEach((pattern, servletName) -> System.out.println("  " + pattern + " -> " + servletName));
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;

        System.out.println("=================================");
        System.out.println("第三阶段标准Servlet Web服务器启动成功！");
        System.out.println("监听端口: " + PORT);
        System.out.println("静态资源目录: " + WEB_ROOT);
        System.out.println("=================================");
        System.out.println("访问示例:");
        System.out.println("  静态页面: http://localhost:" + PORT + "/");
        System.out.println("  标准Servlet: http://localhost:" + PORT + "/servlet");
        System.out.println("  API接口: http://localhost:" + PORT + "/api");
        System.out.println("  API状态: http://localhost:" + PORT + "/api/status");
        System.out.println("  API时间: http://localhost:" + PORT + "/api/time");
        System.out.println("=================================");

        // 主线程循环接收连接
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new StandardRequestHandler(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("接收连接时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;

        // 销毁Servlet容器
        servletContainer.destroy();

        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("标准Servlet Web服务器已停止");
    }

    /**
     * 标准请求处理器
     */
    private class StandardRequestHandler implements Runnable {
        private final Socket clientSocket;

        public StandardRequestHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    OutputStream outputStream = clientSocket.getOutputStream()) {

                // 解析HTTP请求
                HttpServletRequestImpl request = parseRequest(reader);
                HttpServletResponseImpl response = new HttpServletResponseImpl();

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
        private HttpServletRequestImpl parseRequest(BufferedReader reader) throws IOException {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                throw new IOException("无效的HTTP请求");
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                throw new IOException("无效的请求行格式: " + requestLine);
            }

            HttpServletRequestImpl request = new HttpServletRequestImpl(
                    requestParts[0], requestParts[1], requestParts[2]);

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
        private void handleStaticRequest(HttpServletRequestImpl request, HttpServletResponseImpl response) {
            StaticResourceHandler.StaticResource resource = staticHandler.handleRequest(request.getRequestURI());

            response.setStatus(resource.getStatusCode());
            response.setHeader("Content-Type", resource.getMimeType());
            response.setHeader("Content-Length", String.valueOf(resource.getContentLength()));

            // 对于文本内容，写入PrintWriter
            if (resource.getMimeType().startsWith("text/") ||
                    resource.getMimeType().contains("json") ||
                    resource.getMimeType().contains("xml")) {
                try {
                    response.getWriter().write(new String(resource.getContent()));
                } catch (IOException e) {
                    System.err.println("写入响应内容时发生错误: " + e.getMessage());
                }
            } else {
                // 对于二进制文件，需要特殊处理（这里简化为文本处理）
                try {
                    response.getWriter().write(new String(resource.getContent()));
                } catch (IOException e) {
                    System.err.println("写入二进制响应内容时发生错误: " + e.getMessage());
                }
            }

            System.out.println("静态资源处理: " + request.getRequestURI() + " -> " +
                    resource.getStatusCode() + " " + resource.getReasonPhrase());
        }

        /**
         * 发送HTTP响应
         */
        private void sendResponse(OutputStream outputStream, HttpServletResponseImpl response) throws IOException {
            PrintWriter writer = new PrintWriter(outputStream);

            // 发送状态行
            writer.println("HTTP/1.1 " + response.getStatus() + " " + response.getReasonPhrase());

            // 发送响应头
            for (String headerName : response.getHeaderNames()) {
                writer.println(headerName + ": " + response.getHeader(headerName));
            }

            // 发送Cookie
            for (javax.servlet.http.Cookie cookie : response.getCookies()) {
                writer.println("Set-Cookie: " + cookie.getName() + "=" + cookie.getValue());
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
        StandardWebServer server = new StandardWebServer();

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
