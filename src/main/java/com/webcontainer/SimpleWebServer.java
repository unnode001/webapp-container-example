package com.webcontainer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 第一阶段：基础HTTP服务器
 * 实现一个能接收浏览器请求并返回硬编码HTTP响应的服务器
 */
public class SimpleWebServer {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean running = false;

    public void start() throws IOException {
        serverSocket = new ServerSocket(PORT);
        running = true;

        System.out.println("Web服务器启动成功，监听端口: " + PORT);
        System.out.println("访问地址: http://localhost:" + PORT);

        // 主线程循环接收连接
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                // 为每个连接创建新线程处理（BIO模型）
                new Thread(new RequestHandler(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("接收连接时发生错误: " + e.getMessage());
                }
            }
        }
    }

    public void stop() throws IOException {
        running = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
        System.out.println("Web服务器已停止");
    }

    public static void main(String[] args) {
        SimpleWebServer server = new SimpleWebServer();

        // 添加关闭钩子，优雅停止服务器
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
