# 代码全面评审报告（qiutuan-all-powerful-springboot）

> 评审范围：pom.xml、application.yml、全部 Java 源码（controller/service/aspect/filter/config/manager/utils/domain）、SQL 脚本、logback 配置、测试代码。
> 结论先行：项目结构清晰、AOP 注解体系（权限/限流/防重复）与统一响应/异常是亮点；但存在 **3 个高危问题**（依赖 EOL、认证/密码方案薄弱、若干"半成品"功能实际不可用）和大量死代码，建议按文末 P0→P2 优先级整改。

---

## ① 依赖更新

### 1.1 必须处理（安全 / 已停止维护）

| 依赖 | 当前版本 | 问题 | 建议升级 | 兼容风险 |
|---|---|---|---|---|
| **spring-boot** | 2.7.6 | 2022-11 发布，**OSS 支持已于 2023-11 结束**，不再有社区安全补丁 | **短期：升 2.7.18**（最后补丁版，API 不变，风险极低）；**中期：升 3.3.x/3.4.x LTS** | 升 Boot3 是硬迁移：javax.servlet→jakarta.servlet、javax.annotation→jakarta.annotation（@Resource）、javax.validation→jakarta.validation；mybatis-plus 换 mybatis-plus-spring-boot3-starter；knife4j 换 knife4j-openapi3-jakarta-spring-boot-starter；MySQL 驱动换 com.mysql:mysql-connector-j。JDK17 已满足 Boot3 要求 |
| **mysql-connector-java** | 8.0.31（Boot 管理） | 该坐标已废弃，官方迁移到 com.mysql:mysql-connector-j；8.0.31 偏旧 | com.mysql:mysql-connector-j:8.0.33+ | 仅换坐标+版本，驱动类名不变（com.mysql.cj.jdbc.Driver），零风险 |
| **commons-text** | 1.1（2015 年） | 版本过老；虽不受 Text4Shell（CVE-2022-42889，影响 1.5–1.9）影响，但多年无修复 | 1.12.0+（最新 1.13.0） | 项目仅用 StringEscapeUtils.escapeHtml4，API 稳定，低风险 |
| **jjwt** | 0.9.1 | 依赖 javax.xml.bind（JDK11+ 已移除，运行会 NoClassDefFoundError）；**全项目无任何使用** | **直接删除**；未来若需 JWT 用 0.12.x（jjwt-api/jjwt-impl/jjwt-jackson） | 无 |
| **ansj_seg** | 5.1.6 | 2018 年停更的分词库，**全项目无使用** | **直接删除** | 无 |
| **jedis** | 3.7.0 | spring-boot-starter-data-redis 默认 Lettuce，**全项目无使用** | **直接删除** | 无 |
| **junit** | 4.x（与 starter-test 的 JUnit5 混用） | 测试里 @RunWith(SpringRunner.class)（JUnit4）+ org.junit.jupiter.api.Test（JUnit5）混搭，runner 不匹配 | **删除 junit:junit**，统一用 starter-test 的 junit-jupiter | 需同步修测试代码（见③） |

### 1.2 建议更新（常规升级）

| 依赖 | 当前 | 建议 | 风险 |
|---|---|---|---|
| cos_api | 5.6.89 | 5.6.227+ | API 兼容，低风险 |
| minio | 8.5.13 | 8.5.17+（最新 8.5.x） | 低风险 |
| hutool-all | 5.8.32 | 5.8.38（最新 5.8.x） | 低风险；建议改引 hutool-core/hutool-crypto/hutool-json 等按需模块，减小体积 |
| knife4j-openapi2-spring-boot-starter | 4.4.0 | 4.5.0（Boot2 场景最后一版） | 低风险；升 Boot3 时换 openapi3 starter |
| mybatis-plus | 3.5.9 | 可保持（最新 3.5.12）；**注意 3.5.9 起 jsqlparser 已拆分**，mybatis-plus-jsqlparser 必须保留（分页插件依赖它） | 升 Boot3 时必须同步换 starter 坐标 |
| commons-lang3 | 3.12.0（Boot 管理） | 3.17.0（可选） | 低风险 |
| maven-compiler-plugin | 3.8.1 | 3.13.0（可选） | 低风险 |
| jetbrains annotations | 26.0.1 | 保持 | 升 Boot3 可改用 jakarta.annotation，非必须 |

> ⚠️ 升级顺序建议：先升 Boot 2.7.18 + mysql 驱动 + commons-text（一次发布、零迁移成本）；再规划 Boot3 迁移（单独分支，涉及包名批量替换）。**切勿在 Boot 2.7.6 上继续长跑**。

---

## ② AI 能力：接入方案与选型

结合本项目业务（用户体系 + 文件存储 + WebSocket 实时通道 + Redis 缓存 + 请求日志），AI 可以低成本复用现有基建。

### 2.1 选型对比

| 方案 | 优点 | 缺点 | 适用 |
|---|---|---|---|
| **DeepSeek**（OpenAI 兼容 API） | 便宜、中文强、推理好，SDK/HTTP 即插即用 | 数据出域，需合规评估 | 通用问答/客服/代码生成，首选 |
| **阿里云百炼 Qwen** | 国内合规、稳定、有内容审核配套 | 需云账号与计费管理 | 国内生产环境、需要合规背书 |
| **智谱 GLM** | 国内、中文场景好 | 生态略少 | 备选 |
| **Ollama 本地部署** | 数据不出域、免费、可离线 | 需 GPU 机器，效果弱于大厂 API | 私有数据、demo、内网 |
| vLLM + 开源模型自建 | 可控性强 | 运维/GPU 成本高 | 大规模私有化 |

- **框架**：升 Boot3 后首选 **Spring AI 1.x**（官方，spring-ai-openai / spring-ai-alibaba，提供 ChatClient、流式、向量库抽象）；当前 Boot2.7 阶段零依赖方案是**直接用 RestClient/WebClient 调 OpenAI 兼容接口**（DeepSeek/Qwen/Ollama 同一协议，可统一封装）。
- 不建议自研 prompt 编排框架；场景简单时原生 HTTP 最稳。

### 2.2 结合业务的落地场景（按价值排序）

1. **智能客服/助手（推荐首做）**：复用现有 **WebSocket 通道**做流式对话（/websocket/{key} 或新增 SSE 端点），Redis 存会话上下文（滑动窗口最近 N 条），用户体系做登录鉴权与额度/限流（复用 @RateLimit），request_log 表记录 AI 调用（成本审计）。
2. **文档智能（RAG）**：现有 FileController 上传 PDF/Word/TXT 后 → 解析文本 → 切片 → embedding 入库（Redis Stack / pgvector / ES）→ 检索增强问答。用户文件业务天然适合。
3. **内容安全审核**：对用户简介、头像、上传文件名做"关键词库 + 大模型/云安全 API"双重审核（Qwen-Moderation 或阿里云内容安全）。
4. **智能推荐**：基于 user 表画像做 embedding 相似度（用户量小用 Redis 向量即可）。
5. **开发提效（对脚手架本身）**：AI 生成接口文档（对接 OpenAPI/Knife4j）、自动生成单元测试、代码评审——可在 CI 挂 LLM 审查步骤。

### 2.3 建议架构（改造后目录）

```
ai/
  AiClient.java            // 统一接口：chat(stream?)、embedding，支持 deepseek/qwen/ollama 多供应商
  AiProperties.java        // @ConfigurationProperties(prefix="ai")
  AiChatService.java       // 会话管理（Redis 存取上下文）、额度控制、流式
  controller/AiChatController.java  // SSE/WebSocket 流式输出
```

配置示例：

```yaml
ai:
  provider: deepseek            # deepseek | qwen | ollama
  base-url: https://api.deepseek.com
  api-key: ${AI_API_KEY}        # 不要写死在 yml
  model: deepseek-chat
  chat-timeout: 60s
  daily-quota-per-user: 100      # 每人每日次数，配合 RateLimit 切面
```

核心注意点：
- **流式输出**用 SSE（SseEmitter）或 WebSocket 逐 token 推送；超时/重试/熔断（Resilience4j）必须做。
- **上下文管理**：Redis 存 ai:session:{userId}（最近 10~20 条消息），估算 token 防超限；会话 30 分钟过期。
- **安全**：prompt 注入防护（系统提示词隔离用户输入）、日志只记 token 数与摘要（勿记全量对话）、按用户限流防刷。
- **合规**：对外提供前确认数据出境/备案要求；国内生产建议 Qwen/百炼。

---

## ③ 代码优化（按"怎么做"给出）

### 3.1 安全（P0）

1. **密码存储：MD5+固定盐 → BCrypt**。位置：UserServiceImpl.userRegister/userLogin、UserController.addUser（默认密码）。怎么做：引入 spring-security-crypto，用 BCryptPasswordEncoder.encode/matches；登录时 matches(raw, stored)。MD5 固定盐可被彩虹表+GPU 爆破。顺带把 SALT 从 UserServiceImpl 静态常量移到独立 SecurityConstant，消除 controller 对 impl 的 static import。
2. **注册并发竞态：synchronized(userAccount.intern()) → 数据库唯一索引**。位置：UserServiceImpl.userRegister。怎么做：SQL 加 UNIQUE KEY uk_user_account(user_account)；去掉 intern 锁（跨实例无效、intern 池膨胀），改为捕获 DuplicateKeyException 返回"账号已存在"；方法加 @Transactional。
3. **限流/防重复是全局限流，可被单用户拖垮全站**。位置：RateLimitAspect（key=类名+方法名）、RepeatSubmitAspect（key=token/URI）。怎么做：key 增加**用户 ID 或 IP 维度**（class:method:userId 或 :ip），登录接口尤其必须按 IP/账号限；否则 /user/login 的 30 次/60s 全局额度，攻击者刷 30 次后全站无法登录（可用性攻击）。IP 用 NetUtils.getIpAddress（修复 x-forwarded-for 直接信任问题：只信任网关写入的头）。
4. **跨域 + Session Cookie**。位置：CorsConfig（allowedOriginPatterns("*") + allowCredentials(true)）。怎么做：生产限定真实域名；配置 server.servlet.session.cookie.same-site: lax；登录成功后 request.changeSessionId() 防会话固定。
5. **敏感配置外置**。位置：application.yml（MySQL 明文密码 123456789qt）、FileConstant.COS_HOST（硬编码公网 IP）。怎么做：密码/密钥改环境变量 ${DB_PASSWORD}；.gitignore 增加 application-local.yml；COS_HOST 由当前启用的 FileManager 对应配置生成（见 3.3-8）。
6. **XSS 过滤破坏存储数据且不覆盖 JSON body**。位置：XssHttpServletRequestWrapper（escapeHtml4 转义后入库，前端看到双重编码；@RequestBody JSON 完全不过滤）。怎么做：改为**输出端转义或白名单清洗**（如 Hutool XssUtil），不改入库值；JSON 场景用 Jackson 自定义 deserializer 按字段清洗；响应加 X-Content-Type-Options: nosniff 等安全头。

### 3.2 健壮性 / 异常（P0）

7. **文件上传的临时文件创建是坏的（必现失败）**。位置：FileController.uploadFile → File.createTempFile(filepath, null)，filepath 以 "/" 开头且含多级路径（如 /user_avatar/123/...），会被解析为子目录，父目录不存在 → 每次抛 FileNotFoundException → 上传永远"失败"。怎么做：**不落临时文件**，直接用 multipartFile.getInputStream() 流式上传（CosManager/MinioManager 都支持 InputStream）；若确需临时文件，用 File.createTempFile("upload_", "."+ext) 生成安全前缀。同时清洗 getOriginalFilename()（含 ../、\ 时拒绝或重命名），处理 getSuffix(null) 的 NPE 与后缀大小写（.PNG 被拒）。
8. **MinioManager 吞掉上传异常**。位置：MinioManager.putObject 两个重载 catch(Exception) 后只 log。怎么做：throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败")，让 controller 返回 500 而非假成功；CosManager 统一异常语义。
9. **userRegister 参数为空时返回 null（HTTP 200 空响应体）**。位置：UserController.userRegister。怎么做：改 throw new BusinessException(ErrorCode.PARAMS_ERROR)，与登录接口一致。
10. **请求日志落库同步阻塞 + 双份日志**。位置：RequestLogAspect finally 同步 requestLogService.save（每请求一次 DB 写），与 LogInterceptor（文件日志）+ TraceIdFilter 三层并存。怎么做：改用已有 asyncSave（@Async("asyncExecutor")）；status 区分业务异常（BusinessException 记 40000 而非 500）；补齐 TODO 的 params（序列化参数、脱敏）；以配置开关决定是否写库。
11. **traceId 没进日志**。位置：logback-spring.xml 的 pattern 无 %X{traceId}，TraceIdFilter 写的 MDC 完全没输出。怎么做：pattern 加 [%X{traceId}]；支持透传外部 traceId。
12. **WebSocket 半成品**。位置：WebSocketServer。redisTemplate 字段无注入（@ServerEndpoint 实例由容器创建、不走 Spring 注入），sendOneMessageWithAck 必 NPE——删除或改用 SpringConfigurator；WEB_SOCKETS 与 SESSION_POOL 双容器重复——只留一个；onOpen/onClose 空 catch、多处 printStackTrace——改 log.error；心跳 fixedRate=30000 全量遍历——改用 setMaxIdleTimeout 或只对空闲连接发 ping；连接无鉴权——握手校验登录态；equals/hashCode 把 logger、redisTemplate 纳入——只比较 key。
13. **排序字段校验是黑名单，且常量有坑**。位置：SqlUtils.validSortField（黑名单）+ CommonConstant.SORT_ORDER_DESC = " descend"（**值带前导空格**，equals 必然 false）。怎么做：改为**实体字段白名单**（Set.of("id","createTime",...)）；SORT_ORDER_DESC 去掉空格；sortOrder 判空后再比较。
14. **全局异常处理日志噪音**。位置：GlobalExceptionHandler.handleBusinessException 用 log.error + 全栈。怎么做：业务异常降为 log.warn（不打印堆栈，仅记 message+code），系统异常保留 error 全栈。

### 3.3 性能 / 数据层（P1）

15. **MyBatis-Plus 映射隐患**。User 实体缺 @TableName/@TableId（靠默认命名侥幸匹配）；注释自称"逻辑删除"但无 @TableLogic，实际是物理删除——补注解或用 is_delete 逻辑删除。application.yml 的 map-underscore-to-camel-case: false 是雷区（自定义 SQL 结果映射会失效）——改 true。实体时间字段 java.sql.Timestamp 与 MyBatisPlusConfig 自动填充的 LocalDateTime 不匹配，自动填充对 User 静默失效——统一 LocalDateTime。UserQueryRequest 的 unionId/mpOpenId 表里无对应列，查询会报 unknown column——删字段或补列。listUserByPage（管理员）未判空 body 且无 size 上限——补空判、统一上限。
16. **每次请求查库拿登录用户**。位置：getLoginUser/getLoginUserPermitNull 每次都 getById。怎么做：Session 只存**脱敏 DTO（不含密码）**；或加 Redis 缓存（userId→User，5 分钟）。
17. **数据层索引**。user 表 user_name like 无索引（数据量大后补）；user_account 加唯一索引（见 3.1-2）；request_log 已有 idx_create_time，配合 30 天清理可加分区或按月归档。

### 3.4 可读性 / 结构（P1）

18. **包名笔误：servicec 与 service 并存**——把 servicec（User）并入 service。
19. **aop 与 aspect 两个包职责重叠**（Auth/Log 在 aop，RateLimit/RepeatSubmit/RequestLog 在 aspect）——合并为一个。
20. **common/websocket 放错位置**——独立 websocket 包。
21. **重复工具类**：自定义 utils/StringUtils（从未使用）、FileUtils（从未使用）与 hutool/commons-lang3 重复——删除，统一用 org.apache.commons.lang3.StringUtils 与 hutool。
22. **EncryptInterceptor+EncryptConfig 是未启用的半成品**：EncryptConfig 缺 @Configuration，密钥硬编码占位符 your_secret_key_16，afterCompletion 里 throw BusinessException 是错误用法（响应已提交）。二选一：完整实现（密钥走配置、AES/GCM、只加密特定接口）或整体删除——建议删除。
23. **DTO 未用 Bean Validation**：spring-boot-starter-validation 已引入但全项目 0 个校验注解——给 DTO 加 @NotBlank/@Size 并用 @Valid，删除 controller 手写校验（正好利用 GlobalExceptionHandler 的校验分支）。
24. **重复的线程池/调度配置**：ThreadPoolConfig 的 businessExecutor、scheduledExecutor 从未使用（@Async 只用 asyncExecutor），ScheduleConfig 又定义了 taskScheduler——删两个无用 Bean，只留 asyncExecutor + taskScheduler；@EnableScheduling 只留一处。

### 3.5 测试（P1）

25. QiutuanAllPowerfulSpringbootApplicationTests：删 @RunWith(SpringRunner.class)（JUnit4 runner 与 JUnit5 混用）；@Resource(name="FileManager") 应为 fileManager（小写 f）；测试里硬编码的 C:\\Users\\qiutu\\... 本地路径与注释掉的旧 API 调用（minioManager.upload 已不存在）全部删除；改为 mock 或测试专用 bucket。

### 3.6 其他

26. RequestLog 实体 id 建议 @TableId(type = IdType.AUTO)；27. FileController 的 multipart 上限 100MB 与业务上限 10MB 不一致——统一 10MB；28. RedisConfig 的 cacheManager 与 redisCacheConfiguration 两个 Bean 重复且后者未被使用（前缀配置失效）——合并；29. JsonConfig Long→String 全局序列化是好实践，保持；30. 启动横幅 System.out.println 改 log 输出或移除。

---

## ④ 清理冗余（删除清单）

### 4.1 无用依赖（见①表）
jjwt 0.9.1、ansj_seg 5.1.6、jedis 3.7.0、junit:junit（JUnit4）——全部删除。spring-boot-starter-validation 若暂不落地校验注解也删（推荐是落地校验而非删）。

### 4.2 死代码 / 从未被调用的类与方法
- utils/StringUtils.java（与 commons-lang3 重名冲突，0 引用）
- utils/FileUtils.java（0 引用，hutool 已覆盖）
- utils/DateUtils.java（0 引用）
- utils/NetUtils.java（0 引用；若做 IP 限流则"复活"并修 x-forwarded-for）
- utils/EncryptUtils.java（0 引用，与 EncryptInterceptor 重复）
- constant/HttpStatus.java（0 引用，BaseResponse 用的是 ErrorCode）
- constant/CommonConstant.SORT_ORDER_DESC（0 引用，且值带前导空格）
- ThreadPoolConfig 的 businessExecutor、scheduledExecutor Bean
- UserService.getLoginUserPermitNull（接口+实现，0 调用）
- FileManager.putObject(String, String) 重载（Cos/Minio 都实现但无人调用）
- WebSocketServer 的 sendAllMessage/sendMoreMessage/sendOneMessageWithAck、MessageWrapper（无调用方；修复或删除）
- RedisConfig.redisCacheConfiguration Bean（冗余）
- EncryptConfig + EncryptInterceptor（未注册的占位功能，删除或补全）
- RateLimitAspect 中未使用的 RedisTemplate import

### 4.3 注释代码 / 无效配置
- 测试类中的注释代码块与旧 API 调用（System.out.println(minioManager.upload(file)) 等）
- application.yml：明文数据库密码（改环境变量）、map-underscore-to-camel-case: false（改 true）、log-impl: StdOutImpl 生产应移除（上线即删 SQL 打印）
- FileConstant.COS_HOST 硬编码 IP（改为按当前 Manager 动态生成）
- README.md 项目结构章节已过时（servicec 笔误、缺 aspect/filter 目录）
- .idea/workspace.xml、dataSources.local.xml（含本机数据库连接信息）确认未提交到 git；若已提交则从仓库移除（.gitignore 已含 .idea，请核对）

### 4.4 冗余结构建议
- 两个日志记录器（LogInterceptor 文件日志 + RequestLogAspect DB 日志）按 3.2-10 合并
- WEB_SOCKETS + SESSION_POOL 双容器合一
- servicec/service 双包合一；aop/aspect 双包合一

---

## ⑤ 优先级清单

| 优先级 | 事项 |
|---|---|
| **P0（上线前必须）** | ① Boot 2.7.18 + mysql 驱动 + commons-text 升级；② 密码 BCrypt；③ 注册唯一索引；④ 限流/防重复加用户维度；⑤ 修文件上传 createTempFile 崩溃；⑥ MinioManager 异常上抛；⑦ 敏感配置环境变量化；⑧ userRegister null 返回修复 |
| **P1（近期）** | Boot3 迁移评估、traceId 进日志、请求日志异步化、WebSocket 修复/裁剪、MP 注解补齐（@TableName/@TableLogic/类型统一）、DTO 校验注解、死代码清理、CORS/SameSite |
| **P2（规划）** | AI 对话/RAG/内容审核落地（②方案）、分页与索引治理、单元测试补全、README 更新 |

> 备注：以上版本信息以 2025 年初公开渠道为准；执行升级前请用 mvn versions:display-dependency-updates 复核最新版本号，并跑一遍 mvn test 回归。
