# Spring Boot 4.0.1 与 JDK 25 迁移说明

## 版本升级

本项目已从 Spring Boot 3.1.4 + JDK 17 升级到 **Spring Boot 4.0.1 + JDK 25**

### 主要变更

#### 1. Spring Boot 版本
- **旧版本**: Spring Boot 3.1.4
- **新版本**: Spring Boot 4.0.1
- **Spring Framework**: 自动升级到 7.x

#### 2. JDK 版本
- **旧版本**: JDK 17
- **新版本**: JDK 25
- **项目专用 JDK 路径**: `C:\Users\Administrator\Desktop\ServerSync\jdk-25.0.1`

#### 3. Maven 配置
- Maven Compiler Plugin: 3.13.0
- Maven Surefire Plugin: 3.5.2
- 配置了项目专用的 JDK 25 路径

#### 4. 依赖更新

##### API 文档工具迁移
- **移除**: Springfox (不兼容 Spring Boot 4)
  - `springfox-swagger2` 3.0.0
  - `springfox-swagger-ui` 3.0.0
- **新增**: SpringDoc OpenAPI 2.7.0
  - `springdoc-openapi-starter-webmvc-ui`
  - API 文档访问地址从 `/swagger-ui.html` 变更为 `/swagger-ui/index.html`

##### MyBatis-Plus 升级
- **旧版本**: `mybatis-plus-boot-starter` 3.5.3
- **新版本**: `mybatis-plus-spring-boot3-starter` 3.5.9
- 支持 Jakarta EE 11 和 Spring Boot 4

##### 其他依赖更新
- PostgreSQL 驱动: 42.6.0 → 42.7.4
- OSHI Core: 6.4.3 → 6.6.5
- Fastjson2: 2.0.40 → 2.0.54
- Commons Codec: 1.15 → 1.17.1
- JNA: 5.13.0 → 5.15.0
- Jsoup: 1.15.3 → 1.18.3

#### 5. 代码迁移

##### Swagger 注解迁移
原有的 Springfox 注解需要迁移到 SpringDoc OpenAPI:

**旧注解**:
```java
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Api(tags = "服务监控", description = "获取服务信息")
@ApiOperation(value = "获取JVM信息", notes = "返回 JVM 内存信息")
```

**新注解**:
```java
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "服务监控", description = "获取服务信息")
@Operation(summary = "获取JVM信息", description = "返回 JVM 内存信息")
```

#### 6. 启动脚本更新

所有 CMD 脚本已更新以使用项目专用的 JDK 25:
- `A启动.cmd` - 启动应用
- `A编译.cmd` - 编译项目
- `B更新依赖.cmd` - 更新 Maven 依赖

每个脚本都会：
1. 首先检查项目指定的JDK路径是否存在：`C:\Users\Administrator\Desktop\ServerSync\jdk-25.0.1`
2. 如果存在，使用该路径
3. 如果不存在，回退到系统的JAVA_HOME环境变量
4. 如果都不存在，显示错误并退出

#### 7. Maven 配置文件

新增 `.mvn` 目录配置文件:
- `.mvn/maven.config` - Maven 构建参数
- `.mvn/toolchains.xml` - Maven 工具链配置（指定 JDK 25 路径）

**工具链配置方式**：
Maven 通过 toolchains.xml 管理 JDK 路径。如果您的 JDK 不在默认路径，需要：
1. 确保 JDK 25 在 `C:\Users\Administrator\Desktop\ServerSync\jdk-25.0.1`
2. 或者更新 `.mvn/toolchains.xml` 中的 `<jdkHome>` 路径
3. 或者在 `~/.m2/toolchains.xml` 中配置全局工具链

## 构建和运行

### 前置条件

#### 选项 1: 使用项目指定的 JDK 路径（推荐）
1. 将 JDK 25 安装到指定路径：`C:\Users\Administrator\Desktop\ServerSync\jdk-25.0.1`
2. 确保 Maven 3.6+ 已安装
3. 运行 CMD 脚本时会自动检测并使用该 JDK

#### 选项 2: 使用系统环境变量
1. 安装 JDK 25 到任意位置
2. 设置系统环境变量 `JAVA_HOME` 指向 JDK 25 安装目录
3. 更新 `.mvn/toolchains.xml` 中的 `<jdkHome>` 路径（或删除该文件）
4. CMD 脚本会在指定路径不存在时回退到 JAVA_HOME

### 编译项目
```cmd
A编译.cmd
```
或
```cmd
mvn clean package -DskipTests
```

### 启动应用
```cmd
A启动.cmd
```
或
```cmd
mvn spring-boot:run
```

### 更新依赖
```cmd
B更新依赖.cmd
```
或
```cmd
mvn clean install -U
```

## API 文档访问

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 注意事项

1. **Jakarta EE 11**: Spring Boot 4 使用 Jakarta EE 11，所有 `javax.*` 包已迁移到 `jakarta.*`
2. **JDK 25 兼容性**: 所有依赖已更新到支持 JDK 25 的版本
3. **API 文档**: Swagger UI 的访问路径已变更，注意更新相关文档或前端配置
4. **Spring Framework 7**: 自动升级到 Spring Framework 7.x，享受性能和功能改进
5. **虚拟线程**: JDK 25 完全支持虚拟线程（Project Loom），可考虑在高并发场景启用

## 兼容性说明

- **最低 JDK 版本**: JDK 17
- **推荐 JDK 版本**: JDK 21 或 JDK 25
- **支持周期**: Spring Boot 4.0.x 支持到 2026 年底（开源支持）

## 问题排查

如遇到编译或运行问题：

1. 确认 JDK 25 路径正确
2. 清理 Maven 缓存: `mvn clean`
3. 更新依赖: `mvn clean install -U`
4. 检查 JAVA_HOME 环境变量是否正确设置
5. 确保 Maven 使用正确的 JDK 版本: `mvn -v`
