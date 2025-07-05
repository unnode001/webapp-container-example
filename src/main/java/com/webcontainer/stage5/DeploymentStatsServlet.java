package com.webcontainer.stage5;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 部署统计API Servlet
 * 提供Web应用部署和运行状态的监控信息
 */
public class DeploymentStatsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter writer = response.getWriter();

        try {
            // 获取部署器实例（这里需要从应用上下文或其他方式获取）
            // 简化实现，直接构造响应

            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"timestamp\": \"").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                    .append("\",\n");
            json.append("  \"server\": \"多Web应用容器服务器\",\n");
            json.append("  \"version\": \"1.0.0-stage5\",\n");
            json.append("  \"containerStatus\": \"running\",\n");
            json.append("  \"features\": {\n");
            json.append("    \"webAppIsolation\": true,\n");
            json.append("    \"dynamicDeployment\": true,\n");
            json.append("    \"hotReload\": true,\n");
            json.append("    \"classLoaderIsolation\": true,\n");
            json.append("    \"multiContextSupport\": true\n");
            json.append("  },\n");
            json.append("  \"deployment\": {\n");
            json.append("    \"webAppsDirectory\": \"webapps\",\n");
            json.append("    \"scanInterval\": \"5 seconds\",\n");
            json.append("    \"autoDeployEnabled\": true\n");
            json.append("  },\n");
            json.append("  \"endpoints\": {\n");
            json.append("    \"contextDemo\": \"/context\",\n");
            json.append("    \"apiRoot\": \"/api\",\n");
            json.append("    \"sessionDemo\": \"/session\",\n");
            json.append("    \"sessionStats\": \"/api/sessions\"\n");
            json.append("  },\n");
            json.append("  \"message\": \"第五阶段：Web应用隔离与动态部署功能已就绪\"\n");
            json.append("}");

            writer.println(json.toString());

            System.out.println("部署统计API请求处理完成");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writer.println("{\"error\": \"" + e.getMessage() + "\"}");
            System.err.println("DeploymentStatsServlet处理请求时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("DeploymentStatsServlet 初始化完成");
    }

    @Override
    public void destroy() {
        System.out.println("DeploymentStatsServlet 销毁");
        super.destroy();
    }
}
