# API Documentation Access Changes

## Swagger/OpenAPI 迁移说明

由于从 Springfox 迁移到 SpringDoc OpenAPI，API 文档的访问地址已更改。

### 访问地址变更

#### 旧版本 (Springfox)
- Swagger UI: `http://localhost:8090/swagger-ui.html`
- API 文档: `http://localhost:8090/v2/api-docs`

#### 新版本 (SpringDoc OpenAPI)
- Swagger UI: `http://localhost:8090/swagger-ui/index.html` 或 `http://localhost:8090/swagger-ui.html` (自动重定向)
- OpenAPI JSON: `http://localhost:8090/v3/api-docs`
- OpenAPI YAML: `http://localhost:8090/v3/api-docs.yaml`

### 主要改进

1. **更现代的界面**: SpringDoc 提供更新的 Swagger UI 界面
2. **OpenAPI 3.0**: 支持最新的 OpenAPI 3.0 规范
3. **更好的 Spring Boot 4 集成**: 原生支持 Spring Boot 4 和 Jakarta EE 11
4. **自动配置**: 大部分配置自动完成，无需手动配置 Docket

### 注解变更映射

| Springfox (旧) | SpringDoc (新) | 说明 |
|---------------|---------------|------|
| `@Api` | `@Tag` | API 标签 |
| `@ApiOperation` | `@Operation` | 操作描述 |
| `@ApiModel` | `@Schema` | 模型描述 |
| `@ApiModelProperty` | `@Schema` | 属性描述 |
| `@ApiParam` | `@Parameter` | 参数描述 |
| `@ApiResponse` | `@ApiResponse` | 响应描述 |

### 配置说明

SpringDoc OpenAPI 通过依赖 `springdoc-openapi-starter-webmvc-ui` 自动配置。

如需自定义配置，可以在 `application.yml` 中添加：

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
  show-actuator: false
```

当前项目使用 Java 配置方式（SwaggerConfig.java）定义 OpenAPI 信息。
