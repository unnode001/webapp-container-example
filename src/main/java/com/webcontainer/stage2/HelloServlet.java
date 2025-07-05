package com.webcontainer.stage2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.webcontainer.HttpRequest;
import com.webcontainer.HttpResponse;

/**
 * 示例Servlet：Hello Servlet
 * 演示动态内容生成
 */
public class HelloServlet implements MyServlet {

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        // 获取当前时间
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 生成动态HTML内容
        String htmlContent = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Hello Servlet</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; text-align: center; margin-top: 50px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; }\n"
                +
                "        .container { background: rgba(255,255,255,0.1); padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }\n"
                +
                "        h1 { color: #fff; margin-bottom: 20px; }\n" +
                "        .info { background: rgba(255,255,255,0.2); padding: 15px; border-radius: 5px; margin: 10px 0; }\n"
                +
                "        .time { font-size: 18px; color: #f1c40f; }\n" +
                "        a { color: #3498db; text-decoration: none; }\n" +
                "        a:hover { text-decoration: underline; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>🎉 Hello from Servlet!</h1>\n" +
                "        <div class=\"info\">\n" +
                "            <p>这是一个动态生成的页面</p>\n" +
                "            <p class=\"time\">当前时间: " + currentTime + "</p>\n" +
                "        </div>\n" +
                "        <div class=\"info\">\n" +
                "            <p><strong>请求信息：</strong></p>\n" +
                "            <p>请求方法: " + request.getMethod() + "</p>\n" +
                "            <p>请求路径: " + request.getUri() + "</p>\n" +
                "            <p>协议版本: " + request.getProtocol() + "</p>\n" +
                "            <p>User-Agent: " + request.getHeader("User-Agent") + "</p>\n" +
                "        </div>\n" +
                "        <p><a href=\"/\">← 返回首页</a></p>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";

        // 设置响应
        response.setStatus(200, "OK");
        response.addHeader("Content-Type", "text/html; charset=UTF-8");
        response.addHeader("Content-Length", String.valueOf(htmlContent.getBytes().length));
        response.setBody(htmlContent);
    }

    @Override
    public void init() throws Exception {
        System.out.println("HelloServlet 初始化完成");
    }

    @Override
    public void destroy() {
        System.out.println("HelloServlet 销毁");
    }
}
