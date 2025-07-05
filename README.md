# 轻量级Web容器

从零构建的轻量级Web容器（应用服务器），分五个阶段逐步实现现代Web服务器的核心功能。

## 🎯 项目概述

本项目是一个完整的Web容器教学实现，采用"协议为本，逐层抽象"的设计理念，分为五个渐进式开发阶段：

### 开发阶段

| 阶段 | 功能特性 | 核心技术 | 状态 |
|-----|---------|---------|------|
| **第一阶段** | 基础HTTP服务器 | TCP监听、HTTP解析、多线程BIO | ✅ 完成 |
| **第二阶段** | 静态与动态资源 | 静态文件服务、自定义Servlet | ✅ 完成 |
| **第三阶段** | 标准Servlet API | javax.servlet规范、生命周期管理 | ✅ 完成 |
| **第四阶段** | 会话管理与并发优化 | HttpSession、Cookie、线程池 | ✅ 完成 |
| **第五阶段** | Web应用隔离与动态部署 | ClassLoader隔离、热加载、多应用 | ✅ 完成 |

## 🚀 快速开始

### 前置要求

- **JDK 11+** 或更高版本
- **Maven 3.6+** 或更高版本

### 一键启动（推荐）

```bash
# 编译打包
mvn clean package

# 启动第五阶段完整功能服务器
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.stage5.MultiWebAppServer
```

### 分阶段启动

#### 第一阶段：基础HTTP服务器

```bash
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.SimpleWebServer
```

#### 第二阶段：增强Web服务器

```bash
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.stage2.EnhancedWebServer
# 或使用批处理脚本
start-stage2.bat
```

#### 第三阶段：标准Servlet服务器

```bash
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.stage3.StandardWebServer
# 或使用批处理脚本
start-stage3.bat
```

#### 第四阶段：会话管理服务器

```bash
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.stage4.EnhancedWebServer
# 或使用批处理脚本
start-stage4.bat
```

#### 第五阶段：多Web应用容器

```bash
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.stage5.MultiWebAppServer
# 或使用批处理脚本
start-stage5.bat
```

## 🌟 功能特性

### ✅ 已实现功能

#### 🔧 第一阶段：基础HTTP服务器

- TCP连接监听（端口8080）
- HTTP/1.1协议解析（请求行、请求头）
- HTTP响应构建（状态行、响应头、响应体）
- 多线程BIO模型（一连接一线程）
- 基础HTML响应

#### 🚀 第二阶段：静态与动态资源

- 静态资源服务（HTML、CSS、JS、图片等）
- MIME类型自动识别
- 自定义MyServlet接口设计
- 基于反射的Servlet动态加载
- URI到Servlet的映射机制
- 二进制文件支持
- 错误页面处理（404、500等）
- 路径安全检查（防止路径遍历攻击）

#### 📋 第三阶段：标准Servlet API

- 完整的javax.servlet规范实现
- HttpServletRequest/HttpServletResponse标准接口
- Servlet生命周期管理（init、service、destroy）
- ServletContainer容器管理
- 标准化的API接口设计
- JSON格式API响应

#### 👤 第四阶段：会话管理与并发优化

- HttpSession会话对象实现
- Cookie支持和会话跟踪
- SessionManager会话管理器
- 会话自动过期和清理机制
- ThreadPoolExecutor线程池模型
- 会话统计和监控API
- 并发处理性能优化

#### 🏗️ 第五阶段：Web应用隔离与动态部署

- WebAppClassLoader独立类加载器
- Context容器实现应用隔离
- WebAppDeployer动态部署器
- 热加载和自动发现机制
- 多Web应用并存支持
- 应用间完全隔离
- 部署状态监控和统计

## 🌐 访问示例

启动服务器后，可以访问以下端点：

### 核心功能

- **首页**: <http://localhost:8080/>
- **静态资源**: <http://localhost:8080/about.html>
- **动态Servlet**: <http://localhost:8080/hello> （第二阶段）

### 标准Servlet API

- **标准Servlet**: <http://localhost:8080/servlet>
- **API接口**: <http://localhost:8080/api>
- **API状态**: <http://localhost:8080/api/status>
- **API时间**: <http://localhost:8080/api/time>

### 会话管理

- **会话演示**: <http://localhost:8080/session>
- **会话统计**: <http://localhost:8080/api/sessions>
- **会话列表**: <http://localhost:8080/api/sessions/list>
- **会话清理**: <http://localhost:8080/api/sessions/cleanup>

### Web应用隔离

- **Context演示**: <http://localhost:8080/context>
- **部署统计**: <http://localhost:8080/api/deployment>
- **测试应用**: <http://localhost:8080/testapp/> （动态部署）

## 📁 项目结构

```
webapp-container-example/
├── docs/
│   └── WebAppContainer开发流程.md     # 详细开发流程文档
├── src/
│   ├── main/java/com/webcontainer/
│   │   ├── SimpleWebServer.java       # 第一阶段：基础HTTP服务器
│   │   ├── RequestHandler.java        # 请求处理器
│   │   ├── HttpRequest.java           # HTTP请求封装
│   │   ├── HttpResponse.java          # HTTP响应封装
│   │   ├── stage2/                    # 第二阶段：静态与动态资源
│   │   │   ├── EnhancedWebServer.java
│   │   │   ├── StaticResourceHandler.java
│   │   │   ├── MyServlet.java
│   │   │   └── HelloServlet.java
│   │   ├── stage3/                    # 第三阶段：标准Servlet API
│   │   │   ├── StandardWebServer.java
│   │   │   ├── HttpServletRequestImpl.java
│   │   │   ├── HttpServletResponseImpl.java
│   │   │   ├── ServletContainer.java
│   │   │   ├── StandardServlet.java
│   │   │   └── ApiServlet.java
│   │   ├── stage4/                    # 第四阶段：会话管理与并发优化
│   │   │   ├── EnhancedWebServer.java
│   │   │   ├── HttpSessionImpl.java
│   │   │   ├── SessionManager.java
│   │   │   ├── ThreadPoolRequestProcessor.java
│   │   │   ├── EnhancedHttpServletRequestImpl.java
│   │   │   ├── EnhancedHttpServletResponseImpl.java
│   │   │   ├── SessionDemoServlet.java
│   │   │   └── SessionStatsServlet.java
│   │   └── stage5/                    # 第五阶段：Web应用隔离与动态部署
│   │       ├── MultiWebAppServer.java
│   │       ├── WebAppClassLoader.java
│   │       ├── WebAppContext.java
│   │       ├── WebAppDeployer.java
│   │       ├── ContextAwareRequest.java
│   │       ├── ContextDemoServlet.java
│   │       └── DeploymentStatsServlet.java
│   └── test/java/com/webcontainer/     # 单元测试
│       ├── HttpRequestTest.java
│       └── HttpResponseTest.java
├── webapps/                           # Web应用目录
│   ├── ROOT/                          # 根应用
│   │   ├── index.html                 # 首页
│   │   ├── about.html                 # 关于页面
│   │   └── style.css                  # 样式表
│   └── testapp/                       # 测试应用（动态部署演示）
│       └── index.html
├── start-stage2.bat                   # 第二阶段启动脚本
├── start-stage3.bat                   # 第三阶段启动脚本
├── start-stage4.bat                   # 第四阶段启动脚本
├── start-stage5.bat                   # 第五阶段启动脚本
├── pom.xml                           # Maven配置
└── README.md                         # 项目说明
```

## 🧪 测试验证

### 功能测试脚本

```bash
# 基础功能测试
curl http://localhost:8080/                    # 首页
curl http://localhost:8080/about.html          # 静态资源
curl http://localhost:8080/api                 # API接口

# 会话管理测试
curl -c cookies.txt http://localhost:8080/session          # 创建会话
curl -b cookies.txt http://localhost:8080/session          # 使用会话
curl http://localhost:8080/api/sessions                    # 会话统计

# Context隔离测试
curl http://localhost:8080/context                         # Context演示
curl http://localhost:8080/api/deployment                  # 部署统计
```

### 压力测试

```bash
# 使用Apache Benchmark进行并发测试
ab -n 1000 -c 50 http://localhost:8080/
ab -n 500 -c 20 http://localhost:8080/api/status
```

## 🔧 技术实现细节

### 核心技术栈

- **Java 11+**: 主要开发语言
- **Socket编程**: TCP连接和HTTP协议实现
- **多线程**: BIO模型和线程池优化
- **反射机制**: 动态Servlet加载
- **类加载器**: 应用隔离机制
- **Maven**: 项目构建和依赖管理

### 设计模式应用

- **工厂模式**: Servlet实例创建
- **单例模式**: Servlet实例管理
- **观察者模式**: 会话监听机制
- **装饰器模式**: Request/Response包装
- **策略模式**: 不同阶段的处理策略

### 性能优化

- **线程池**: 替代一连接一线程模型
- **对象复用**: Servlet实例单例管理
- **资源管理**: 自动关闭和清理机制
- **异步处理**: 部署器后台扫描
- **内存优化**: 及时释放无用资源

## 📚 学习价值

### 适合人群

- Java Web开发初学者
- 希望深入理解Web容器原理的开发者
- 计算机科学专业学生
- 对底层技术感兴趣的程序员

### 学习收获

- **HTTP协议深度理解**: 从底层实现HTTP/1.1协议
- **Servlet规范掌握**: 完整实现javax.servlet API
- **并发编程实践**: 多线程和线程池应用
- **类加载机制**: ClassLoader隔离和热加载
- **Web容器原理**: 理解Tomcat等容器的核心实现

## 🚧 已知问题与改进

### 当前限制

- 仅支持HTTP/1.1协议（不支持HTTP/2）
- 简化的web.xml解析（主要通过代码配置）
- 基础的安全机制（主要防护路径遍历）
- 内存存储（Session不支持持久化）

### 改进方向

- 添加HTTPS支持
- 实现完整的web.xml解析
- 添加Filter过滤器支持
- 实现JSP编译和执行
- 添加WebSocket支持
- 实现Session持久化

## 🤝 贡献指南

本项目欢迎任何形式的贡献：

1. **Bug报告**: 发现问题请创建Issue
2. **功能建议**: 欢迎提出改进建议
3. **代码贡献**: 提交Pull Request
4. **文档改进**: 完善文档和注释

### 开发环境设置

```bash
git clone <repository-url>
cd webapp-container-example
mvn clean install
```

## 📄 许可证

本项目采用 **MIT许可证**，详情请参见 [LICENSE](LICENSE) 文件。

## 📞 联系方式

- 项目维护者：[您的姓名]
- Email：[your.email@example.com]
- GitHub：[your-github-username]

---

⭐ 如果这个项目对您有帮助，请给我们一个Star！
