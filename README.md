# JavaFrame

前后端分离的空框架，只有骨架，不含业务。后端 `JavaFrame` 提供 HTTP 接口，前端 `javaframeff` 提供页面。

## 包含什么

- 统一返回外壳 `{code, message, data}`，HTTP 状态与业务码双层
- 参数校验，一次返回全部字段错误
- Argon2id 密码哈希
- JWT 访问票（30 分钟）+ 刷新票（32 字节随机数，只存 SHA-256）
- 刷新票一次性使用，用过即废
- 每请求 `X-Trace-Id`，写进 MDC 和响应头
- Actuator 只暴露 health、info
- 虚拟线程、优雅停机
- dev / prod 两套配置
- Docker Compose 一键启动

不含：用户表、数据库、角色权限、业务接口。这些是骨架搭好之后才加的东西。

## 技术栈

| 部分 | 版本 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.1 |
| springdoc | 3.1.1 |
| jjwt | 0.12.6 |
| Vue | 3.5 |
| Vite | 8 |
| Node | 24 |

## 本地启动

需要 JDK 21 和 Node 24。

后端：

```powershell
cd JavaFrame
.\mvnw.cmd spring-boot:run
```

前端另开一个终端：

```powershell
cd javaframeff
npm install
npm run dev
```

后端在 http://localhost:8080 ，前端在 http://localhost:5173 。前端通过 Vite 代理把 `/api` 转给后端，所以浏览器里不会有跨域问题。

演示账号：`admin` / `admin123`。

接口文档（仅 dev）：http://localhost:8080/swagger-ui.html

## 环境变量

| 变量 | 用途 | 默认值（仅 dev） |
|---|---|---|
| `APP_AUTH_USERNAME` | 演示账号 | `admin` |
| `APP_AUTH_PASSWORD` | 演示密码 | `admin123` |
| `APP_JWT_SECRET` | JWT 签名密钥，至少 32 字符 | 开发用固定值 |
| `SPRING_PROFILES_ACTIVE` | 运行环境 | `dev` |

`prod` profile 下前三个变量**没有默认值**。缺任何一个，启动会直接失败并报 `Could not resolve placeholder`。这是故意的：宁可起不来，也不要拿开发口令上线。

## Docker

需要 Docker Desktop 已启动，且能访问 Docker Hub（国内通常要配镜像加速器）。

在仓库根目录建 `.env`：

```text
APP_AUTH_USERNAME=admin
APP_AUTH_PASSWORD=admin123
APP_JWT_SECRET=compose-only-secret-change-me-32b
```

然后：

```powershell
docker compose up --build
```

前端在 http://localhost:8088 ，后端在 http://localhost:8080 。生产 profile 下 Swagger 是关的，这是正确的。

停止：

```powershell
docker compose down
```

`.env` 已在根目录 `.gitignore` 里，不会被提交。

## 接口

| 方法 | 路径 | 是否需要登录 |
|---|---|---|
| GET | `/api/v1/health` | 否 |
| POST | `/api/v1/auth/login` | 否 |
| POST | `/api/v1/auth/refresh` | 否 |
| GET | `/api/v1/me` | 是 |
| GET | `/actuator/health` | 否 |
| GET | `/actuator/info` | 否 |

登录和刷新返回：

```json
{ "code": 0, "message": "success", "data": { "token": "...", "refreshToken": "..." } }
```

刷新请求体：

```json
{ "refreshToken": "..." }
```

成功响应的 HTTP 状态是 200。失败响应的 HTTP 状态不是 200，正文仍是统一外壳：

| 场景 | HTTP | code |
|---|---|---|
| 参数不正确 | 400 | 40000 |
| 账号或密码错误 | 401 | 40100 |
| 未登录 / 访问票无效 | 401 | 40100 |
| 刷新票无效或已使用 | 401 | 40101 |
| 路径不存在 | 404 | 40400 |
| 方法不允许 | 405 | 40500 |
| 未处理异常 | 500 | 50000 |

参数校验失败时，`data.fields` 里是每个字段的具体原因：

```json
{
  "code": 40000,
  "message": "请求参数不正确",
  "data": {
    "fields": [
      { "field": "username", "message": "用户名不能为空" },
      { "field": "password", "message": "密码不能为空" }
    ]
  }
}
```

## 测试

```powershell
cd JavaFrame
.\mvnw.cmd test
```

14 个测试，覆盖：健康检查、未登录 401、字段校验、密码错误、404、405、刷新票轮换、刷新票过期、演示账号校验。

前端类型检查：

```powershell
cd javaframeff
npm run type-check
```

## 目录

```text
JavaFrame/src/main/java/org/example/javaframe
├── common        Result、FieldErrors、ErrorCode、BizException、GlobalExceptionHandler、TraceIdFilter
├── config        WebConfig（跨域）、OpenApiConfig、SecurityConfig
├── auth          LoginRequest、RefreshRequest、DemoUser、JwtService、JwtAuthenticationFilter、RefreshTokenStore
└── controller    HealthController、AuthController、MeController

javaframeff/src
├── api           types.ts、request.ts、refresh.ts、auth.ts、health.ts
├── router        index.ts（只有 / 一条路由）
└── views         HomeView.vue
```

## 已知设计选择

**刷新票存在内存里。** 用的是 `ConcurrentHashMap`，后端一重启全部失效，需要重新登录。单机、空框架阶段够用。要跨重启保持登录，或者要开多台后端，得把它挪到数据库或 Redis。

**只有一个路由。** 前端只有 `/`，登录框和当前用户都在这一页。没有登录页、没有路由守卫。加业务页面时再补。

**前端循环依赖已拆开。** `refresh.ts` 用原生 axios，不引用 `request.ts`；`request.ts` 只引用 `refresh.ts`；`auth.ts` 引用 `request.ts`。依赖是单向的，别改回去。

**Actuator 索引页已关。** `management.endpoints.web.discovery.enabled: false`，所以 `/actuator` 返回 401 而不是端点列表。health 和 info 单独在白名单里。

## 下一步能加什么

- 数据库：Flyway 建表，把 `DemoUser` 换成真用户表
- 登出接口：删除刷新票指纹，再清浏览器两张票
- 登录限流：防字典攻击
- Prometheus：`micrometer-registry-prometheus`
- 过期刷新票清理：定时任务扫一遍
