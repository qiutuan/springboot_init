![SpringBoot](https://img.shields.io/badge/SpringBoot-3.5-orange)![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-ORM框架-orange)![Sa-Token](https://img.shields.io/badge/Sa--Token-认证授权-orange)![SpringAI](https://img.shields.io/badge/SpringAI-1.0-AI-orange)![Redis](https://img.shields.io/badge/Redis-缓存-orange)![WebSocket](https://img.shields.io/badge/WebSocket-实时通信-orange)![MinIO](https://img.shields.io/badge/MinIO-对象存储-orange)![腾讯云COS](https://img.shields.io/badge/腾讯云COS-对象存储-orange)![AOP](https://img.shields.io/badge/AOP-面向切面编程-orange)

# SpringBoot 脚手架

## 项目介绍

功能齐全的 Spring Boot 3.5 企业级脚手架：Sa-Token + RBAC 权限、Spring AI（通义千问）对话与 RAG、对象存储、WebSocket、Redis 缓存与限流、请求日志。

## 主要特性

### 1. 认证与权限（Sa-Token + RBAC）
- 登录认证：Sa-Token（token 持久化到 Redis）
- 角色权限：RBAC 模型（sys_role / sys_permission / sys_user_role / sys_role_permission）
- 注解鉴权：`@SaCheckLogin` / `@SaCheckRole("admin")` / `@SaCheckPermission("user:list")`
- 密码加密：BCrypt（替换原 MD5）

### 2. AI 能力（Spring AI 1.0 + Spring AI Alibaba / DashScope）
- 非流式对话：`POST /api/ai/chat`（多轮上下文持久化到 Redis）
- 流式对话：`POST /api/ai/chat/stream`（SSE）
- RAG 问答：`POST /api/ai/rag/chat`（知识库检索 + 大模型回答）
- 仅检索：`POST /api/ai/rag/retrieve`（返回命中知识片段）
- 内置 Advisor：提示注入防护、调用日志、多轮上下文、RAG 检索增强

### 3. 接口保护
- 接口限流 `@RateLimit`（按 用户ID/IP 维度）
- 防重复提交 `@RepeatSubmit`
- 请求日志（异步落库）+ 日志链路 TraceId

### 4. 文件存储
- MinIO / 腾讯云 COS 流式上传（`file.manager` 切换）
- 按业务/用户分目录，文件名防穿越清洗

### 5. 实时通信
- WebSocket 单点/广播 + 心跳

## 快速开始

### 环境要求
- JDK 17+、Maven 3.6+
- MySQL 8+（执行 `src/main/resources/sql/springboot_init.sql`）
- Redis 6+

### 配置（环境变量）
| 变量 | 说明 |
|---|---|
| `DB_PASSWORD` | MySQL 密码 |
| `REDIS_HOST` / `REDIS_PASSWORD` | Redis 地址/密码 |
| `DASHSCOPE_API_KEY` | 通义千问 API Key（AI 功能需要，RAG 启动时做 embedding） |
| `MINIO_ENDPOINT` 等 | 对象存储 |

### 登录示例
```bash
# 注册/登录后，响应头 satoken 即登录 token
curl -X POST http://localhost:8800/user/login \
  -H "Content-Type: application/json" \
  -d '{"userAccount":"test","userPassword":"12345678"}'

# 携带 token 访问受保护接口
curl http://localhost:8800/user/get/login -H "satoken: <token>"
```

### AI 调用示例
```bash
# 多轮对话（conversationId 相同即同一会话，上下文在 Redis）
curl -X POST http://localhost:8800/api/ai/chat \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"message":"你好，介绍一下你自己","conversationId":"demo-1"}'

# 流式（SSE）
curl -N -X POST http://localhost:8800/api/ai/chat/stream \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"message":"讲个笑话","conversationId":"demo-1"}'

# RAG 问答（知识库在 ./data/knowledge 或 classpath:/knowledge）
curl -X POST http://localhost:8800/api/ai/rag/chat \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"question":"这个项目的技术栈是什么？"}'

# 仅检索
curl -X POST http://localhost:8800/api/ai/rag/retrieve \
  -H "Content-Type: application/json" -H "satoken: <token>" \
  -d '{"question":"如何配置 AI 接口？"}'
```

## 项目结构

```
├── annotation   // 自定义注解（限流/防重复提交）
├── aspect       // AOP 切面（日志/限流/防重复）
├── ai           // AI 能力（对话/RAG/Advisor/知识库初始化）
├── common       // 通用类（统一响应/分页）
├── config       // 配置类（含 Sa-Token 拦截器）
├── constant     // 常量
├── controller   // 控制器
├── domain       // 实体/DTO/VO/枚举
├── exception    // 全局异常处理
├── filter       // 过滤器（TraceId/XSS）
├── manager      // 外部服务封装（对象存储）
├── mapper       // MyBatis-Plus Mapper（含 RBAC 查询）
├── security     // Sa-Token 权限数据源（StpInterface）
├── service      // 业务服务
├── utils        // 工具类
└── websocket    // WebSocket 服务
```
