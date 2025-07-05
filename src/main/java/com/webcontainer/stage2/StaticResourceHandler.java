package com.webcontainer.stage2;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 第二阶段：静态资源处理器
 * 负责处理静态文件请求（HTML, CSS, JS, 图片等）
 */
public class StaticResourceHandler {
    private final String webRoot;
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        // 初始化MIME类型映射
        MIME_TYPES.put(".html", "text/html; charset=UTF-8");
        MIME_TYPES.put(".htm", "text/html; charset=UTF-8");
        MIME_TYPES.put(".css", "text/css; charset=UTF-8");
        MIME_TYPES.put(".js", "application/javascript; charset=UTF-8");
        MIME_TYPES.put(".json", "application/json; charset=UTF-8");
        MIME_TYPES.put(".txt", "text/plain; charset=UTF-8");
        MIME_TYPES.put(".xml", "application/xml; charset=UTF-8");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
        MIME_TYPES.put(".gif", "image/gif");
        MIME_TYPES.put(".ico", "image/x-icon");
        MIME_TYPES.put(".svg", "image/svg+xml");
    }

    public StaticResourceHandler(String webRoot) {
        this.webRoot = webRoot;
        // 确保webRoot目录存在
        File webRootDir = new File(webRoot);
        if (!webRootDir.exists()) {
            webRootDir.mkdirs();
        }
    }

    /**
     * 处理静态资源请求
     * 
     * @param uri 请求的URI
     * @return StaticResource对象，包含文件内容和MIME类型
     */
    public StaticResource handleRequest(String uri) {
        try {
            // 处理根路径，默认返回index.html
            if ("/".equals(uri)) {
                uri = "/index.html";
            }

            // 构建完整的文件路径
            Path filePath = Paths.get(webRoot, uri).normalize();

            // 安全检查：防止路径遍历攻击
            if (!filePath.startsWith(Paths.get(webRoot).normalize())) {
                return createErrorResource(403, "Forbidden");
            }

            File file = filePath.toFile();

            // 检查文件是否存在
            if (!file.exists() || !file.isFile()) {
                return createErrorResource(404, "Not Found");
            }

            // 检查文件是否可读
            if (!file.canRead()) {
                return createErrorResource(403, "Forbidden");
            }

            // 读取文件内容
            byte[] content = Files.readAllBytes(filePath);
            String mimeType = getMimeType(file.getName());

            return new StaticResource(200, "OK", content, mimeType, file.getName());

        } catch (IOException e) {
            System.err.println("读取静态资源时发生错误: " + e.getMessage());
            return createErrorResource(500, "Internal Server Error");
        }
    }

    /**
     * 根据文件扩展名获取MIME类型
     */
    private String getMimeType(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "application/octet-stream";
        }

        String extension = fileName.substring(lastDot).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, "application/octet-stream");
    }

    /**
     * 创建错误响应资源
     */
    private StaticResource createErrorResource(int statusCode, String reasonPhrase) {
        String errorPage = createErrorPage(statusCode, reasonPhrase);
        byte[] content = errorPage.getBytes();
        return new StaticResource(statusCode, reasonPhrase, content, "text/html; charset=UTF-8", "error.html");
    }

    /**
     * 创建错误页面HTML
     */
    private String createErrorPage(int statusCode, String reasonPhrase) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>" + statusCode + " " + reasonPhrase + "</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; text-align: center; margin-top: 100px; }\n" +
                "        h1 { color: #e74c3c; }\n" +
                "        p { color: #7f8c8d; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>" + statusCode + " " + reasonPhrase + "</h1>\n" +
                "    <p>请求的资源不可用</p>\n" +
                "    <p><a href=\"/\">返回首页</a></p>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * 静态资源封装类
     */
    public static class StaticResource {
        private final int statusCode;
        private final String reasonPhrase;
        private final byte[] content;
        private final String mimeType;
        private final String fileName;

        public StaticResource(int statusCode, String reasonPhrase, byte[] content, String mimeType, String fileName) {
            this.statusCode = statusCode;
            this.reasonPhrase = reasonPhrase;
            this.content = content;
            this.mimeType = mimeType;
            this.fileName = fileName;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getReasonPhrase() {
            return reasonPhrase;
        }

        public byte[] getContent() {
            return content;
        }

        public String getMimeType() {
            return mimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public long getContentLength() {
            return content.length;
        }
    }
}
