package com.webcontainer.stage2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.webcontainer.HttpRequest;
import com.webcontainer.HttpResponse;

/**
 * 第二阶段：增强Web服务器
 * 支持静态资源和动态Servlet处理
 */
public class EnhancedWebServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = "webapps/ROOT";

    private ServerSocket serverSocket;
    private boolean running = false;
    private StaticResourceHandler staticHandler;
    private Map<String, String> servletMappings;
    private Map<String, MyServlet> servletInstances;

    public EnhancedWebServer() {
        this.staticHandler = new StaticResourceHandler(WEB_ROOT);
        this.servletMappings = new HashMap<>();
        this.servletInstances = new HashMap<>();

        // 初始化Servlet映射
        initServletMappings();
    }

    /**
     * 初始化Servlet映射关系
     */
    private void initServletMappings() {
        // 硬编码的映射关系（第三阶段将改为web.xml配置）
        servletMappings.put("/hello", HelloServlet.class.getName());
        servletMappings.put("/api/hello", HelloServlet.class.getName());

        System.out.println("Servlet映射初始化完成:");
        servletMappings.forEach((path, className) -> System.out.println("  " + path + " -> " + className));
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;

        System.out.println("=================================");
        System.out.println("第二阶段增强Web服务器启动成功！");
        System.out.println("监听端口: " + PORT);
        System.out.println("静态资源目录: " + WEB_ROOT);
        System.out.println("=================================");
        System.out.println("访问示例:");
        System.out.println("  静态页面: http://localhost:" + PORT + "/");
        System.out.println("  动态内容: http://localhost:" + PORT + "/hello");
        System.out.println("=================================");

        // 主线程循环接收连接
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                new Thread(new EnhancedRequestHandler(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("接收连接时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;

        // 销毁所有Servlet实例
        servletInstances.values().forEach(MyServlet::destroy);
        servletInstances.clear();

        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("增强Web服务器已停止");
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
                HttpRequest request = parseRequest(reader);
                HttpResponse response = new HttpResponse();

                // 根据URI决定处理方式
                if (isServletRequest(request.getUri())) {
                    handleServletRequest(request, response);
                } else {
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
         * 判断是否为Servlet请求
         */
        private boolean isServletRequest(String uri) {
            return servletMappings.containsKey(uri);
        }

        /**
         * 处理Servlet请求
         */
        private void handleServletRequest(HttpRequest request, HttpResponse response) {
            String uri = request.getUri();
            String servletClassName = servletMappings.get(uri);

            if (servletClassName == null) {
                response.setStatus(404, "Not Found");
                response.addHeader("Content-Type", "text/html; charset=UTF-8");
                response.setBody("<h1>404 - Servlet Not Found</h1>");
                return;
            }

            try {
                MyServlet servlet = getServletInstance(servletClassName);
                servlet.service(request, response);
                System.out.println("Servlet处理完成: " + uri + " -> " + servletClassName);
            } catch (Exception e) {
                System.err.println("Servlet处理错误: " + e.getMessage());
                e.printStackTrace();

                response.setStatus(500, "Internal Server Error");
                response.addHeader("Content-Type", "text/html; charset=UTF-8");
                response.setBody("<h1>500 - Internal Server Error</h1><p>" + e.getMessage() + "</p>");
            }
        }

        /**
         * 获取Servlet实例（单例模式）
         */
        private MyServlet getServletInstance(String className) throws Exception {
            MyServlet servlet = servletInstances.get(className);
            if (servlet == null) {
                // 使用反射创建Servlet实例
                Class<?> servletClass = Class.forName(className);
                servlet = (MyServlet) servletClass.getDeclaredConstructor().newInstance();
                servlet.init();
                servletInstances.put(className, servlet);
                System.out.println("创建新的Servlet实例: " + className);
            }
            return servlet;
        }

        /**
         * 处理静态资源请求
         */
        private void handleStaticRequest(HttpRequest request, HttpResponse response) {
            StaticResourceHandler.StaticResource resource = staticHandler.handleRequest(request.getUri());

            response.setStatus(resource.getStatusCode(), resource.getReasonPhrase());
            response.addHeader("Content-Type", resource.getMimeType());
            response.addHeader("Content-Length", String.valueOf(resource.getContentLength()));

            // 对于二进制内容，我们需要特殊处理
            if (resource.getMimeType().startsWith("text/") || resource.getMimeType().contains("json")
                    || resource.getMimeType().contains("xml")) {
                response.setBody(new String(resource.getContent()));
            } else {
                // 对于二进制文件，直接设置字节数组
                response.setBinaryBody(resource.getContent());
            }

            System.out.println("静态资源处理: " + request.getUri() + " -> " + resource.getStatusCode() + " "
                    + resource.getReasonPhrase());
        }

        /**
         * 解析HTTP请求（复用第一阶段的代码）
         */
        private HttpRequest parseRequest(BufferedReader reader) throws IOException {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                throw new IOException("无效的HTTP请求");
            }

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length != 3) {
                throw new IOException("无效的请求行格式: " + requestLine);
            }

            HttpRequest request = new HttpRequest(requestParts[0], requestParts[1], requestParts[2]);

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
         * 发送HTTP响应
         */
        private void sendResponse(OutputStream outputStream, HttpResponse response) throws IOException {
            PrintWriter writer = new PrintWriter(outputStream);

            // 发送状态行
            writer.println(response.getStatusLine());

            // 发送响应头
            for (String header : response.getHeaders()) {
                writer.println(header);
            }

            // 发送空行
            writer.println();
            writer.flush();

            // 发送响应体
            if (response.getBinaryBody() != null) {
                // 二进制内容
                outputStream.write(response.getBinaryBody());
            } else if (response.getBody() != null) {
                // 文本内容
                writer.print(response.getBody());
                writer.flush();
            }
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
