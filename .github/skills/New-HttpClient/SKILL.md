---
name: New-HttpClient
description: 如果需要新建java.net.http.HttpClient有关的文件时，使用此技巧.
---

按照以下操作:

1. **配置读取**：如果需要引入 `config.properties` 内的变量，从 `config/ConfigManager.java` 读取。
2. **HttpClient 复用**：调用 `config/HttpClientFactory.java`，使用配置好的 HttpClient 实例，避免重复创建。
3. **依赖注入**：如需在 Spring Bean 中使用 HttpClient，通过构造器注入获取依赖，不使用 `@Autowired` 字段注入。
4. **日志规范**：使用 SLF4J，日志变量统一命名为 `log`。
5. **异常处理**：HTTP 调用需进行异常捕获并记录日志，对外返回友好的错误信息。
