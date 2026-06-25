# CloudBlog — 微服务博客系统

基于 Spring Cloud Alibaba 的微服务博客系统，支持用户注册登录、博客发布、点赞评论、文件上传等功能。

---

## 项目简介

CloudBlog 是一个采用微服务架构的多人博客系统，前端使用 Vue 3 + Element Plus，后端基于 Spring Cloud Alibaba 技术栈。项目拆分为 5 个微服务，通过 Nacos 实现服务注册与发现，通过 Gateway 统一对外暴露 API，使用 Sentinel 做流量控制和熔断保护。

### 功能特性

- 用户注册/登录（JWT 鉴权）
- 博客发布、编辑、删除、分页浏览
- 博客点赞/取消点赞
- 评论与回复
- 头像上传（MinIO 文件存储）
- Nacos 配置中心热更新
- Sentinel 流量控制与熔断降级

---

## 微服务作业要求

本项目满足以下微服务课程核心要求：

| 要求 | 实现方式 |
|------|----------|
| **服务拆分** | 项目拆分为 5 个独立微服务 + 1 个网关，职责分明，可独立部署 |
| **服务注册与发现** | 使用 Nacos 作为注册中心，所有服务启动后自动注册 |
| **配置中心** | 使用 Nacos Config，支持配置动态刷新 |
| **API 网关** | 使用 Spring Cloud Gateway，统一路由、鉴权、跨域处理 |
| **远程调用** | 使用 OpenFeign 实现服务间通信（如 blog-service 调用 user-service 获取用户信息） |
| **负载均衡** | 集成 Spring Cloud LoadBalancer，默认按轮询策略分发请求 |
| **熔断降级** | 使用 Sentinel 进行流量控制、熔断降级，gateway 层也配置了 Sentinel |
| **前后端分离** | Vue 3 前端通过 API 调用与后端交互 |
| **容器化部署** | 提供 Dockerfile + docker-compose 一键部署方案 |

---

## 项目架构及原理

### 系统架构图

```
┌─────────────┐      ┌──────────────────────────────────────┐
│  浏览器/Vue  │─────▶│        Nginx / Vite Dev Server       │
│  前端页面    │      │    (http://localhost:3000)            │
└─────────────┘      └────────────┬─────────────────────────┘
                                  │ /api/*
                                  ▼
┌─────────────────────────────────────────────────────────┐
│              Spring Cloud Gateway (8080)                 │
│         路由转发 · JWT 鉴权 · CORS · Sentinel             │
└────┬────────┬────────┬────────┬────────┬────────────────┘
     │        │        │        │        │
     ▼        ▼        ▼        ▼        ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌─────────────┐
│  user  │ │  blog  │ │comment │ │  file  │ │    Nacos    │
│ service│ │ service│ │service │ │service │ │  注册中心    │
│  8081  │ │  8082  │ │  8083  │ │  8084  │ │   8848      │
└───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘ └─────────────┘
    │          │          │          │
    └──────────┴──────────┴──────────┘
              │
              ▼
┌──────────────────────────────────────┐
│      MySQL 8.0 · Redis 7 · MinIO     │
│       (Docker 中间件层)               │
└──────────────────────────────────────┘
```

### 请求处理流程

1. **用户请求** → 前端页面发起 AJAX 请求到 `/api/*`
2. **Vite 代理转发** → 开发环境将请求转发到 Gateway（生产环境由 Nginx 转发）
3. **Gateway 鉴权** → `JwtAuthGlobalFilter` 检查请求路径是否在白名单中，否则验证 JWT Token，解析出 `userId` 放入请求头 `X-User-Id`
4. **路由转发** → Gateway 根据路径前缀（`/api/user/**`、`/api/blog/**` 等）路由到对应微服务
5. **服务处理** → 各微服务处理业务逻辑，必要时通过 Feign 调用其他服务
6. **统一响应** → 所有响应封装为统一格式 `Result<T>`：`{ code, msg, data }`

### 关键组件原理

#### 服务注册与发现（Nacos）
各微服务启动时向 Nacos 注册自身地址和端口，Gateway 通过服务名（`lb://user-service`）实现负载均衡调用。当服务实例上下线时，Nacos 实时通知消费者。

#### JWT 鉴权
用户登录成功后，user-service 签发 JWT Token。前端将 Token 存入 localStorage，每次请求通过 `Authorization: Bearer <token>` 携带。Gateway 的 `JwtAuthGlobalFilter` 解析 Token 并将用户 ID 透传至下游服务。

#### 服务间通信（OpenFeign）
以 blog-service 为例：显示博客时需要作者昵称，blog-service 通过 Feign 声明式调用 user-service 的接口获取用户信息。Feign 结合 LoadBalancer 实现自动服务发现和负载均衡。

#### 配置中心（Nacos Config）
blog-service 集成了 Nacos Config，可在 Nacos 控制台修改配置（如博客分页大小），服务端实时生效，无需重启。

#### 熔断与限流（Sentinel）
Sentinel 在 Gateway 层和服务层都配置了流控规则。当某个接口 QPS 超过阈值时，直接返回降级响应 `{"code":503,"msg":"服务繁忙，请稍后重试"}`，防止雪崩效应。

---

## 技术栈

### 后端
| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 基础框架 |
| Spring Cloud Alibaba | 2021.0.6.0 | 微服务全家桶 |
| Nacos | 2.2.3 | 注册中心 + 配置中心 |
| Spring Cloud Gateway | - | API 网关 |
| Sentinel | 1.8.6 | 熔断降级与流量控制 |
| OpenFeign | - | 声明式服务调用 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7 | 缓存（点赞数据） |
| MinIO | - | 对象存储（文件/头像） |
| JWT (jjwt) | 0.12.3 | 身份认证 |

### 前端
| 组件 | 版本 |
|------|------|
| Vue 3 | ^3.4 |
| Vite | ^5.1 |
| Element Plus | ^2.5 |
| Pinia | ^2.1 |
| Axios | ^1.6 |
| Vue Router | ^4.3 |

---

## 快速启动

### 前置条件
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose
- Node.js 18+

### 启动步骤

```bash
# 1. 启动中间件（MySQL/Redis/Nacos/Sentinel/MinIO）
docker compose up -d

# 2. ⚠️ 导入 Nacos 配置（重要！Nacos 启动后必须先导入配置，否则 blog-service 启动报错）
#    方式一：使用导入脚本（推荐，需 curl）
bash nacos-config/import-config.sh

#    方式二：手动创建（无需 curl，浏览器操作）
#    打开 http://localhost:8848/nacos → 配置管理 → 配置列表
#    点击"+"号新建配置：
#      Data ID: blog-service.yaml
#      分组: DEFAULT_GROUP
#      配置格式: YAML
#      配置内容: 复制 nacos-config/blog-service.yaml 的完整内容

# 3. 编译项目
mvn install -DskipTests

# 4. 启动微服务（按顺序，各开一个终端）
mvn spring-boot:run -pl user-service       # 8081
mvn spring-boot:run -pl blog-service       # 8082（需先完成第 2 步）
mvn spring-boot:run -pl comment-service    # 8083
mvn spring-boot:run -pl file-service       # 8084
mvn spring-boot:run -pl cloud-gateway      # 8080（最后启动）

# 5. 启动前端
cd frontend && npm install && npm run dev  # 3000

# 6. 访问 http://localhost:3000
```

> **首次运行必须完成第 2 步**。Nacos 是全新实例，不导入 `blog-service.yaml` 会导致 blog-service 启动失败。  
> 配置导入成功后，所有功能可用，包括 Nacos Config 配置中心热更新。以后重新启动无需再次导入。

### 一键启动

```bash
# 自动完成编译 → Docker → 导入 Nacos 配置 → 启动微服务 → 启动前端
bash start-all.sh
```

> 一键启动脚本会自动执行 `import-config.sh` 导入 Nacos 配置。如果导入失败（如未安装 curl），会提示手动导入，脚本继续执行。此时 blog-service 会因找不到配置而启动失败，需完成手动导入后单独重启。

### Docker 部署

```bash
mvn clean install -DskipTests
docker compose build
docker compose up -d
```

---

## 测试方案

### 1. 功能测试（接口级别）

使用 curl 或 Postman 对各服务接口进行功能验证：

| 测试项 | 方法 | 接口 | 预期结果 |
|--------|------|------|----------|
| 注册 | POST | `/api/user/register` | 返回成功，创建用户 |
| 登录 | POST | `/api/user/login` | 返回 JWT Token 和用户信息 |
| 获取用户 | GET | `/api/user/{id}` | 返回用户信息 |
| 修改资料 | PUT | `/api/user/profile` | 需 Token，返回更新后的用户 |
| 创建博客 | POST | `/api/blog` | 需 Token，返回博客信息 |
| 博客分页 | GET | `/api/blog/page` | 白名单，无需登录 |
| 点赞 | POST | `/api/blog/{id}/like` | 需 Token，切换点赞状态 |
| 评论 | POST | `/api/comment` | 需 Token，创建评论 |
| 上传文件 | POST | `/api/file/upload` | 返回文件 URL |
| 用户博客 | GET | `/api/blog/page?userId=X` | 只返回该用户的博客 |

### 2. 集成测试（服务间通信）

测试 blog-service 调用 user-service 获取博客作者信息：
- 创建博客 → 查询博客详情 → 验证返回的作者昵称与头像是否正确
- 测试方式：通过网关 GET `/api/blog/{id}`，查看返回数据中的 `authorNickname` 字段

### 3. 网关鉴权测试

| 测试项 | 请求 | 预期结果 |
|--------|------|----------|
| 白名单接口 | GET `/api/user/1` | 无需 Token，正常返回 |
| 非白名单接口无 Token | POST `/api/blog` 不带 Token | 返回 401 |
| 非白名单接口有 Token | POST `/api/blog` 带有效 Token | 正常创建 |
| 过期 Token | 使用过期 Token 请求 | 返回 401 |

### 4. 配置中心热更新测试

1. 登录 Nacos 控制台 `http://localhost:8848/nacos`
2. 修改 blog-service 的配置（如 `blog.default-page-size`）
3. 无需重启服务，调用博客分页接口验证新配置已生效

### 5. 熔断与限流测试（Sentinel）

1. 登录 Sentinel 控制台 `http://localhost:8858`
2. 为某个接口设置 QPS 阈值（如 5）
3. 使用 JMeter 或 curl 快速发送超过阈值的请求
4. 观察超出阈值的请求返回 `503 服务繁忙` 降级响应

### 6. 压力测试（JMeter）

项目已包含 JMeter 测试脚本 `jmeter/CloudBlog_Stress_Test.jmx`，测试内容：

| 场景 | 线程数 | 说明 |
|------|--------|------|
| 用户登录并发 | 50 | 模拟 50 用户同时登录 |
| 博客分页并发 | 100 | 模拟 100 用户同时浏览博客列表 |
| 创建博客并发 | 30 | 模拟 30 用户同时发布博客 |
| 点赞并发 | 50 | 模拟 50 用户同时点赞 |

运行压力测试：
```bash
jmeter -n -t jmeter/CloudBlog_Stress_Test.jmx -l logs/results.jtl -e -o jmeter/report
```

### 7. 前端功能测试

| 测试项 | 操作 | 验证点 |
|--------|------|--------|
| 注册流程 | 填写用户名/密码/昵称 → 提交 | 注册成功提示，自动切换到登录 |
| 登录流程 | 输入账号密码 → 登录 | 跳转到公共博客页，导航栏显示昵称 |
| 博客列表 | 浏览公共博客 | 分页正常，显示标题/作者/时间 |
| 发布博客 | 填写标题/内容 → 发布 | 博客出现在个人列表中 |
| 编辑博客 | 点击编辑 → 修改内容 → 保存 | 更新后的内容可见 |
| 删除博客 | 点击删除 → 确认 | 博客从列表中消失 |
| 点赞 | 点击点赞按钮 | 图标变蓝，计数+1；再点取消 |
| 评论 | 输入评论 → 回车 | 评论出现在评论区 |
| 浏览他人博客 | 点击用户头像/昵称 | 跳转到该用户的博客列表 |
| 修改资料 | 编辑昵称/简介/头像 | 保存后立即生效 |
| 刷新页面 | F5 刷新任意页面 | 登录状态保持，导航栏昵称正常 |

---

## 项目结构

```
SpringCloudFinal/
├── common/                  # 公共模块（JWT工具、统一响应）
├── user-service/            # 用户服务（8081）
├── blog-service/            # 博客服务（8082）
├── comment-service/         # 评论服务（8083）
├── file-service/            # 文件服务（8084）
├── cloud-gateway/           # API 网关（8080）
├── frontend/                # Vue 3 前端
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── api/             # API 封装
│   │   ├── router/          # 路由配置
│   │   ├── store/           # Pinia 状态管理
│   │   ├── components/      # 公共组件
│   │   └── utils/           # 工具（axios 封装）
│   └── Dockerfile           # 前端 Nginx 容器化
├── sql/                     # 数据库建表脚本
├── docker-compose.yml       # Docker 编排（中间件+微服务）
├── jmeter/                  # JMeter 压力测试脚本
├── start-all.sh             # 一键启动脚本
└── pom.xml                  # Maven 父 POM
```

---

## 服务端口一览

| 组件 | 端口 |
|------|------|
| 前端页面 | 3000 |
| API 网关 | 8080 |
| 用户服务 | 8081 |
| 博客服务 | 8082 |
| 评论服务 | 8083 |
| 文件服务 | 8084 |
| MySQL | 3307 |
| Redis | 6379 |
| Nacos | 8848 |
| Sentinel 控制台 | 8858 |
| MinIO API | 9000 |
| MinIO 控制台 | 9001 |
