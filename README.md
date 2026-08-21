# Qiutuan SpringBoot Init Project

功能齐全的 Spring Boot 3.5 企业级脚手架，集成了用户、RBAC权限控制、Spring AI (通义千问) RAG、多策略文件服务、WebSocket 实时通信、Redis缓存和限流防护。

## 🚀 核心能力

### 1. 认证与权限 (Sa-Token + RBAC)
- **会话持久化**：用户登录态及会话信息存储于 Redis，避免重启丢失。
- **RBAC 模型**：支持角色管理、权限管理、用户角色绑定、角色权限绑定。
- **注解鉴权**：支持 `@SaCheckLogin`、`@SaCheckRole`、`@SaCheckPermission` 等。
- **缓存剔除**：角色/权限更新时，自动踢掉对应用户并清除缓存。

### 2. AI 与 RAG (Spring AI)
- **对话服务**：提供非流式（`/api/ai/chat`，Redis多轮会话持久化）与流式接口（`/api/ai/chat/stream`，SSE 形式）。
- **RAG 增强**：基于 VectorStore 实现相似度检索，优化系统 Prompt 抑制 AI 幻觉，当知识库无内容时进行如实退避回复；Prompt 模板支持外置配置。

### 3. 多策略文件服务 (Local / MinIO / COS)
- **策略模式**：解耦底层文件存储逻辑，支持快速切换 `LocalFileManager`、`MinioManager` 和 `CosManager`。
- **本地存储**：内置本地磁盘文件读取、下载与删除，普通用户仅能删除自己上传的文件。
- **格式校验**：内置不同业务（用户头像、普通文件、图片等）的文件大小及后缀格式白名单校验。

### 4. 接口安全与性能
- **并发保护**：支持 `@RateLimit`（基于 Redis Lua 脚本）和防重复提交 `@RepeatSubmit` 注解。
- **日志落库**：异步切面保存请求日志，请求响应包含唯一 TraceId。

---

## 🛠️ 项目结构

```
top.qtcc.qiutuanallpowerfulspringboot
├── annotation   // 并发限制与防刷注解
├── aspect       // AOP 切面（日志拦截、限流处理）
├── ai           // 智能对话、向量检索与知识库初始化
├── common       // 统一返回结果封装与分页定义
├── config       // 跨域、MyBatis-Plus、Redis、Sa-Token 等系统配置
├── constant     // 系统常量定义
├── controller   // 外部接口（用户、文件、AI、RBAC 等）
├── domain       // 数据模型（Entity、DTO、VO、Enum）
├── exception    // 全局异常捕捉与业务异常定义
├── filter       // MDC 链路追踪与过滤器
├── manager      // 多媒介文件存储策略实现
├── mapper       // MyBatis-Plus 数据库操作
├── security     // Sa-Token 角色与权限数据装载源
├── service      // 业务逻辑层 (User, SysRole, RequestLog)
└── websocket    // WebSocket 连接会话管理
```

---

## ⚙️ 环境与配置

### 配置文件说明
- `application.yml`：公共配置（AI 参数、Sa-Token 校验时间等）。
- `application-dev.yml`：开发环境配置（默认使用本地 MySQL、Redis 和 Local 存储，包含 SQL 输出与 API 文档）。
- `application-prod.yml`：生产环境配置（敏感信息一律通过系统环境变量注入，无默认密码，开启 fail-fast 启动自检）。

### 必填环境变量（生产环境）
- `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`：MySQL 数据库连接
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD`：Redis 缓存连接
- `DASHSCOPE_API_KEY`：AI 大模型 API 密钥
- `CORS_ALLOWED_ORIGINS`：跨域允许来源白名单（英文逗号分割，禁止 `*`）

> [!WARNING]
> 为了安全，请勿将真实的账号、密码、API Key 等敏感配置硬编码并提交至 Git 仓库。本地开发建议使用环境变量或创建 `application-local.yml`（已被 `.gitignore` 忽略）进行本地覆盖，生产环境配置必须使用系统环境变量注入。

---

## 🏁 快速开始

### 1. 启动依赖服务
1. **数据库**：启动 MySQL 并执行 `src/main/resources/sql/springboot_init.sql`。
2. **缓存**：启动 Redis 服务。

### 2. 编译与运行
```bash
# 本地编译与构建
mvn clean compile

# 本地开发启动
mvn spring-boot:run

# 生产环境启动
export SPRING_PROFILES_ACTIVE=prod
java -jar target/qiutuan-all-powerful-springboot-0.0.1-SNAPSHOT.jar
```

### 3. API 接口文档
启动项目后本地访问以下地址即可查看 Knife4j 接口文档：
[http://localhost:8800/doc.html](http://localhost:8800/doc.html)
