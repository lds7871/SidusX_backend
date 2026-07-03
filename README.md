# 基于SpringBoot 的面向公众航空航天科普与任务模拟平台的设计与实现

是的没错这是毕业设计项目。
<div align="center">
  <img src="./主页面视图.png" alt="主页面视图" width="80%">
</div>

当今随着世界航天技术不断发展，中国航天进入“空间站时代”，并迈向深空探测，
公众对航天的关注在各方面都达到了前所未有的高度。然而一系列成功的背后公众高涨的求知欲与专业航天知识的高门槛、低可及性之间存在巨大鸿沟。当前的主流科普方式，如新闻报道、纪录片、图文展览，多属于单向、被动、主观性的传播，营销号则更多。所以，一个正确客观的航空航天科普平台是必不可少但也是目前较为空缺的生态位。本文设计并实现了一个基于`Spring Boot`的面向公众航空航天科普与任务平台，其中包含全球航天的实时数据，配有图文科普向的文章以及百科，拥有更加高级感与立体感的航天系列介绍以及配套的`3D`模型展示，还有航天任务拟真模拟等多种功能。系统采用`SpringBoot4+PostgreSQL` 构建后端，`Vue`构建用户端，`JavaFX`构建管理端。用户端采用`Three.js`构建可视化`3D`模型展示和任务模拟框架，让科普效果更显著，信息展示更生动。服务端口配置`WebSocket` 广播与服务器系统级的信息获取，便于管理员快速运维。后端采用新技术与经典三层
架构，性能优秀，扩展灵活。

- [当前后端](https://github.com/lds7871/SidusX_backend) 
- [用户网页端](https://github.com/lds7871/SidusX_web) 
- [管理桌面端](https://github.com/lds7871/SidusX_fx) 

##  项目介绍

毕业设计项目，主要提供以下功能：

- **用户管理** - 用户登录、注册、权限管理
- **Wiki系统** - 知识库编写、管理、评论、审核
- **内容管理** - 文章、公告、新闻等内容的发布与管理  
- **天文数据** - NASA每日图片、火箭发射信息、太空探测器数据
- **信息服务** - 服务器状态监控、API日志记录
- **公开调用** - 定时调用公开接口
- **安全机制** - 项目包含完整的IP/Pass_token二选一机制，并且提供了完整的接口调用记录存储和WebSocket控制台日志输出功能。


## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 25 | 编程语言 |
| Spring Boot | 4.0.1 | Web框架 |
| PostgreSQL | Latest | 关系数据库 |
| MyBatis | 3.0.4 | ORM框架 |
| Lombok | 1.18.40 | 代码生成 |
| SpringDoc OpenAPI | 2.7.0 | API文档 |
| Maven | - | 项目构建 |

##  快速启动

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

3. **修改邮箱配置** 
   ```yaml
   mail:
     smtp:
       username: your_email@qq.com
       password: your_app_password
       from: your_email@qq.com
   ```

4. **配置IP白名单** 
   ```yaml
   security:
     ip-whitelist:
       - "127.0.0.1"
       - "your_ip_address"
   ```
5. **部分接口调用** 
   ```java
    @BypassIpWhitelist // 该注解表示此接口不受IP白名单限制
    //如无此接口需要在API调用时添加Query参数Pass_token=${PASS_TOKENS}
    //{PASS_TOKENS}支持多个令牌逗号分隔，包含检索(如abc，传入abcde也可通过)
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

##  项目结构

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

##  主要API模块

- `WikiController` - Wiki知识库管理
- `UserController` - 用户认证与管理
- `ArticleController` - 文章管理
- `AnnouncementController` - 公告管理
- `NasaDailyImageController` - NASA天文数据
- `RecentLaunchController` - 火箭发射信息
- `MsShipController` - 太空探测器数据
- `DeepSeekController` - AI集成服务
- `ServerInfoController` - 服务器监控

##  系统特性

- ✅ 基于Spring Boot 4 的现代Web框架
- ✅ 多数据源支持 (JPA + MyBatis)
- ✅ IP白名单与令牌绕过安全机制
- ✅ WebSocket控制台日志输出
- ✅ 完整的用户权限管理
- ✅ API日志记录与追踪
- ✅ 定时任务支持

##  许可证

MIT License

