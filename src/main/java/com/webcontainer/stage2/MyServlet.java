package com.webcontainer.stage2;

import com.webcontainer.HttpRequest;
import com.webcontainer.HttpResponse;

/**
 * 第二阶段：自定义的简化Servlet接口
 * 用于分离动态逻辑处理
 */
public interface MyServlet {

    /**
     * 处理HTTP请求
     * 
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @throws Exception 处理过程中可能发生的异常
     */
    void service(HttpRequest request, HttpResponse response) throws Exception;

    /**
     * 初始化Servlet（可选实现）
     */
    default void init() throws Exception {
        // 默认空实现
    }

    /**
     * 销毁Servlet（可选实现）
     */
    default void destroy() {
        // 默认空实现
    }
}
