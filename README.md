# GHstart 

一个功能丰富的内容管理与信息展示平台，集成用户管理、Wiki知识库、文章管理、公告系统、NASA天文数据等多个模块。

## 📋 项目介绍

GHstart 是一个毕业设计项目，主要提供以下功能：

- **用户管理** - 用户登录、注册、权限管理
- **Wiki系统** - 知识库编写、管理、评论、审核
- **内容管理** - 文章、公告、新闻等内容的发布与管理  
- **天文数据** - NASA每日图片、火箭发射信息、太空探测器数据
- **信息服务** - 服务器状态监控、API日志记录
- **AI集成** - DeepSeek AI能力集成

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 25 | 编程语言 |
| Spring Boot | 4.0.1 | Web框架 |
| PostgreSQL | Latest | 关系数据库 |
| MyBatis | 3.0.4 | ORM框架 |
| Spring Data JPA | 4.0.1 | 持久层框架 |
| Lombok | 1.18.40 | 代码生成 |
| SpringDoc OpenAPI | 2.7.0 | API文档 |
| Maven | - | 项目构建 |

## 🚀 快速启动

### 前置要求

- **Java 25** 及以上
- **Maven 3.6+**
- **PostgreSQL 12+** 

### 环境配置

1. **创建本地配置文件**
   ```bash
   # 复制示例配置文件
   cp src/main/resources/application.example.yml src/main/resources/application.yml
   ```

2. **修改数据库连接信息**
   编辑 `src/main/resources/application.yml`，更新以下配置：
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/your_database
       username: your_username
       password: your_password
   ```

3. **修改邮箱配置** （可选）
   ```yaml
   mail:
     smtp:
       username: your_email@qq.com
       password: your_app_password
       from: your_email@qq.com
   ```

4. **配置IP白名单** （可选）
   ```yaml
   security:
     ip-whitelist:
       - "127.0.0.1"
       - "your_ip_address"
   ```

### 编译与启动

```bash
# 清理并编译
mvn clean compile

# 运行项目
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/GHstart-1.0-SNAPSHOT.jar
```

应用启动后，访问 **http://localhost:8100** 即可使用。

## 📦 项目结构

```
src/main/
├── java/LDS/Person/
│   ├── controller/      # 控制层 (12个主要API模块)
│   ├── service/         # 业务逻辑层
│   ├── repository/      # 数据访问层
│   ├── entity/          # 数据实体
│   ├── dto/             # 数据传输对象
│   ├── config/          # 配置类 (安全、邮件、数据库等)
│   ├── util/            # 工具类
│   └── tasks/           # 定时任务
└── resources/
    ├── application.yml  # 应用配置
    ├── logback-spring.xml
    └── mapper/          # MyBatis 映射文件
```

## 💡 主要API模块

- `WikiController` - Wiki知识库管理
- `UserController` - 用户认证与管理
- `ArticleController` - 文章管理
- `AnnouncementController` - 公告管理
- `NasaDailyImageController` - NASA天文数据
- `RecentLaunchController` - 火箭发射信息
- `MsShipController` - 太空探测器数据
- `DeepSeekController` - AI集成服务
- `ServerInfoController` - 服务器监控

## ⚙️ 系统特性

- ✅ 基于Spring Boot 4 的现代Web框架
- ✅ 多数据源支持 (JPA + MyBatis)
- ✅ IP白名单与令牌绕过安全机制
- ✅ WebSocket控制台日志输出
- ✅ 完整的用户权限管理
- ✅ API日志记录与追踪
- ✅ 定时任务支持

## 📝 许可证

MIT License

