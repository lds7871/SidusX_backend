# GHstart - PersonLog 个人日志系统

基于 Spring Boot 4.0.1 + JDK 25 的个人日志管理系统

## 📋 项目信息

- **Spring Boot**: 4.0.1
- **JDK**: 25
- **Maven**: 3.6+
- **数据库**: PostgreSQL
- **ORM**: MyBatis-Plus 3.5.9
- **API 文档**: SpringDoc OpenAPI 3.0

## 🚀 快速开始

### 前置要求

**选项 1: 使用项目指定的 JDK 路径（推荐）**
- 将 JDK 25 安装到: `C:\Users\Administrator\Desktop\ServerSync\jdk-25.0.1`
- Maven 3.6 或更高版本

**选项 2: 使用系统环境变量**
- JDK 25 安装到任意位置
- 设置 `JAVA_HOME` 环境变量指向 JDK 25
- 更新 `.mvn/toolchains.xml` 或删除该文件

### 快速命令

```cmd
# 编译项目
A编译.cmd

# 启动应用
A启动.cmd

# 更新依赖
B更新依赖.cmd

# 同步代码
B同步项目.cmd
```

### 手动构建

```bash
# 清理并编译
mvn clean package -DskipTests

# 启动应用
mvn spring-boot:run

# 更新依赖
mvn clean install -U
```

## 📚 API 文档

启动应用后访问：

- **Swagger UI**: http://localhost:8090/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8090/v3/api-docs
- **OpenAPI YAML**: http://localhost:8090/v3/api-docs.yaml

## 🔧 配置说明

### 数据库配置

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-host:5432/your-database?currentSchema=your-schema
    username: your-username
    password: your-password
```

### 代理配置

编辑 `src/main/resources/config.properties`:

```properties
# 是否启用代理（true/false）
proxy.is.open=true
# 代理主机地址
proxy.host=127.0.0.1
# 代理端口
proxy.port=33210
```

### 安全配置

IP 白名单和 Pass Token 配置在 `application.yml` 中：

```yaml
security:
  ip-whitelist-enabled: true
  ip-whitelist:
    - "127.0.0.1"
    - "your-ip-address"
  pass-token-enabled: true
  pass-tokens:
    - "your-secret-token"
```

## 📁 项目结构

```
Prototypy_Spring/
├── src/
│   ├── main/
│   │   ├── java/LDS/Person/
│   │   │   ├── GHstartApplication.java    # 主启动类
│   │   │   ├── config/                    # 配置类
│   │   │   ├── controller/                # 控制器
│   │   │   ├── service/                   # 服务层
│   │   │   └── jmapper/                   # Mapper 接口
│   │   └── resources/
│   │       ├── application.yml            # 主配置文件
│   │       └── config.properties          # 自定义配置
│   └── test/                              # 测试代码
├── .mvn/                                  # Maven 配置
│   ├── maven.config                       # Maven 参数
│   └── toolchains.xml                     # JDK 工具链配置
├── pom.xml                                # Maven 项目配置
├── A启动.cmd                              # 启动脚本
├── A编译.cmd                              # 编译脚本
├── B更新依赖.cmd                          # 依赖更新脚本
└── 提示.txt                               # 开发提示
```

## 🔄 最近更新

### Spring Boot 4.0.1 迁移 (2025-12-22)

项目已从 Spring Boot 3.1.4 + JDK 17 升级到 Spring Boot 4.0.1 + JDK 25

**主要变更**:
- ✅ Spring Boot 4.0.1 with Spring Framework 7.x
- ✅ JDK 25 support with Jakarta EE 11
- ✅ SpringDoc OpenAPI 3.0 (替代 Springfox)
- ✅ MyBatis-Plus 3.5.9 (spring-boot3-starter)
- ✅ 所有依赖更新到最新兼容版本

**详细信息**:
- [MIGRATION_NOTES.md](MIGRATION_NOTES.md) - 完整迁移指南
- [MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md) - 迁移总结
- [API_DOCS_CHANGES.md](API_DOCS_CHANGES.md) - API 文档变更

## 🛠️ 开发指南

### 添加新的 API 端点

使用 SpringDoc OpenAPI 注解:

```java
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/example")
@Tag(name = "示例", description = "示例 API")
public class ExampleController {
    
    @GetMapping("/hello")
    @Operation(summary = "问候", description = "返回问候消息")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello, World!");
    }
}
```

### 添加新的 DTO

```java
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "示例 DTO")
public class ExampleDTO {
    
    @Schema(description = "ID", example = "1")
    private Long id;
    
    @Schema(description = "名称", example = "示例名称")
    private String name;
}
```

## 🧪 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试
mvn test -Dtest=YourTestClass

# 跳过测试
mvn clean package -DskipTests
```

## 📝 日志

查看应用日志配置在 `application.yml`:

```yaml
logging:
  level:
    root: INFO
    LDS.Person: DEBUG
    org.springframework.web: WARN
```

## 🔐 安全

- ✅ IP 白名单过滤
- ✅ Pass Token 认证
- ✅ HTTPS 支持（可配置）
- ✅ SQL 注入防护（MyBatis-Plus）
- ✅ XSS 防护（Spring Security）

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可

[添加您的许可信息]

## 📞 联系方式

- 开发团队: PersonLog 开发团队
- 项目地址: https://github.com/lds7871/Prototypy_Spring

---

**注意**: 本项目已配置为使用 JDK 25 和 Spring Boot 4.0.1。确保您的开发环境满足这些要求。
