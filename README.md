# 轻量级Web容器

- **JDK 11+** 或更高版本
- **Maven 3.6+** 或更高版本

### 启动命令

```bash
# 编译打包
mvn clean package

# 启动应用服务器
java -cp target/webapp-container-example-1.0.0.jar com.webcontainer.stage5.MultiWebAppServer
```

### 核心功能 && 已实现功能

- **首页**: <http://localhost:8080/>
- **静态资源**: <http://localhost:8080/about.html>
- **动态Servlet**: <http://localhost:8080/hello>

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
- **测试应用**: <http://localhost:8080/testapp/>

## 🧪 测试验证

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

## 开发流程
**应用程序被分为五个阶段性的模块化实现**

| 阶段 | 功能特性 | 核心技术 | 状态 |
|-----|---------|---------|------|
| **第一阶段** | 基础HTTP服务器 | TCP监听、HTTP解析、多线程BIO | ✅ 完成 |
| **第二阶段** | 静态与动态资源 | 静态文件服务、自定义Servlet | ✅ 完成 |
| **第三阶段** | 标准Servlet API | javax.servlet规范、生命周期管理 | ✅ 完成 |
| **第四阶段** | 会话管理与并发优化 | HttpSession、Cookie、线程池 | ✅ 完成 |
| **第五阶段** | Web应用隔离与动态部署 | ClassLoader隔离、热加载、多应用 | ✅ 完成 |

