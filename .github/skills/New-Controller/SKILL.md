---
name: New-Controller
description: 如果需要新建控制层时，需要用到此技巧.
---

按照以下操作:

1. **文件组织**：控制层加入 `controller/` 文件夹，请求和响应 DTO 加入 `dto/request/` 和 `dto/response/` 文件夹，实体加入 `entity/` 文件夹，数据访问加入 `repository/` 文件夹，服务接口写在 `service/` 内，实现写在 `service/impl/` 内。
2. **公开接口注解**：如有说明为"公开接口"，为接口方法加上 `@BypassIpWhitelist` 注解。
3. **DTO 规范**：请求与响应都使用 JSON 格式，均需定义 DTO 类并引入 `lombok.Getter` 与 `lombok.Setter`，使用 `io.swagger.v3.oas.annotations` 组件增强 API 文档可读性。
4. **Swagger 注解**：控制器类必须添加 `@Tag(name = "模块名称", description = "模块描述")` 注解，方法添加 `@Operation(summary = "...")` 注解。
5. **依赖注入**：统一使用**构造器注入**（`private final` 字段 + 构造方法），不使用 `@Autowired` 字段注入。
6. **日志规范**：使用 SLF4J，日志变量统一命名为 `log`，格式为 `private static final Logger log = LoggerFactory.getLogger(XxxController.class);`。
7. **缩进规范**：统一使用 **4 空格**缩进。
8. **CORS**：不要在控制器上添加 `@CrossOrigin`，全局 CORS 已在 `SecurityConfig.java` 中配置。
9. **配置读取**：如果需要引入 `config.properties` 内的变量，从 `config/ConfigManager.java` 读取。
10. **技术栈**：此项目为 JDK 25 + Spring Boot 4.0.1。
11. **分页查询**：使用 `JdbcTemplate` 和 MyBatis 手动完成实现，不使用 MyBatisPlus（不支持 JDK 25）。
12. **接口路径**：新建的接口使用 `/GHapi/` 开头。
13. **响应封装**：通用操作使用 `JsonResponse`（success/failure），特定模块可使用自定义 ResultResponse（如 `UserResultResponse`、`ArticleResultResponse`）。
