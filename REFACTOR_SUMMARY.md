# 控制层与服务层规范化改动总集

## 概述

本次改动对项目的控制层（Controller）、服务层（Service）以及 `.github/skills` 提示词文件进行了统一规范化处理，旨在提升代码一致性和可维护性。

---

## 一、控制层规范化

### 1. 统一依赖注入方式

将所有控制器中的 `@Autowired` 字段注入改为**构造器注入**（`private final` 字段 + 构造方法）。

**涉及文件：**
- `UserController.java`
- `ArticleController.java`
- `ServerInfoController.java`

**改动前：**
```java
@Autowired
private XxxService xxxService;
```

**改动后：**
```java
private final XxxService xxxService;

public XxxController(XxxService xxxService) {
    this.xxxService = xxxService;
}
```

### 2. 补全 `@Tag` Swagger 注解

为缺少 `@Tag` 注解的控制器补全类级别 Swagger 文档注解。

**涉及文件：**
- `WikiController.java` — 新增 `@Tag(name = "Wiki 管理", description = "Wiki 的增删改查接口")`
- `NasaDailyImageController.java` — 新增 `@Tag(name = "NASA 每日图片", description = "NASA APOD 图片的查询与删除接口")`

### 3. 统一日志变量命名

将所有日志变量名统一为 `log`（原部分控制器使用 `logger`）。

**涉及文件：**
- `UserController.java`（`logger` → `log`）
- `ArticleController.java`（`logger` → `log`）

### 4. 统一代码缩进为 4 空格

将原使用 2 空格缩进的文件统一为 4 空格缩进。

**涉及控制器文件：**
- `WikiNewController.java`
- `WikiCommentController.java`
- `NasaDailyImageController.java`
- `RecentLaunchController.java`
- `AnnouncementController.java`

### 5. 清理冗余代码

- `WikiNewController.java` — 移除未使用的 `NameCheckResponse` 内部类（与 `WikiController` 中的重复）
- `DeepSeekController.java` — 移除注释掉的 `@CrossOrigin` 和多处注释掉的日志语句
- `WikiCommentController.java` — 取消注释 `@BypassIpWhitelist` 注解使其生效

### 6. 移除冗余 `@CrossOrigin`

移除 `UserController`、`ArticleController`、`ServerInfoController` 上的 `@CrossOrigin(origins = "*", maxAge = 3600)` 注解，因全局 CORS 已在 `SecurityConfig.java` 中统一配置。

---

## 二、服务层规范化

### 1. 统一代码缩进为 4 空格

**涉及服务接口文件：**
- `AnnouncementService.java`
- `FalconStatsService.java`
- `RecentLaunchService.java`
- `WikiCommentService.java`
- `WikiNewService.java`

**涉及服务实现文件：**
- `AnnouncementServiceImpl.java`
- `FalconStatsServiceImpl.java`
- `RecentLaunchServiceImpl.java`
- `WikiCommentServiceImpl.java`
- `WikiNewServiceImpl.java`

### 2. 补全 JavaDoc 文档注释

- `AnnouncementService.java` — 为 `getLatestAnnouncement()`、`getRecentAnnouncements()` 补全方法级 JavaDoc
- `WikiReviewService.java` — 为所有方法补全 `@param` 和 `@return` 文档注释

### 3. EmailService 规范化

- 将 `@Autowired` 字段注入改为构造器注入
- 日志变量名 `logger` 统一为 `log`

---

## 三、.github/skills 提示词更新

### `New-Controller/SKILL.md`

- 修正 Swagger 注解引用：`io.swagger.annotations` → `io.swagger.v3.oas.annotations`
- 新增以下规范条目：
  - `@Tag` Swagger 类注解要求
  - 构造器注入规范（不使用 `@Autowired`）
  - 日志变量统一命名为 `log`
  - 4 空格缩进规范
  - 不在控制器级别添加 `@CrossOrigin`（已全局配置）
  - 响应封装规范（`JsonResponse` / 自定义 `ResultResponse`）

### `New-HttpClient/SKILL.md`

- 新增构造器注入规范
- 新增日志命名规范
- 新增异常处理规范

---

## 四、规范总结

| 规范项 | 标准 |
|--------|------|
| 依赖注入 | 构造器注入（`private final` + 构造方法） |
| Swagger 类注解 | `@Tag(name = "...", description = "...")` |
| Swagger 方法注解 | `@Operation(summary = "...")` |
| 日志变量名 | `log` |
| 日志框架 | SLF4J (`LoggerFactory.getLogger(Xxx.class)`) |
| 代码缩进 | 4 空格 |
| CORS 配置 | 全局 `SecurityConfig.java`，不在控制器级别配置 |
| 公开接口 | `@BypassIpWhitelist` 注解 |
| 接口路径 | `/GHapi/` 前缀 |
| 分页实现 | JdbcTemplate + MyBatis（不使用 MyBatisPlus） |
