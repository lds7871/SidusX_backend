---
name: New-Controller
description: 如果需要新建控制层时，需要用到此技巧.
---

按照以下操作:

1. 控制层加入controller文件夹，响应和请求加入dto的request\response文件夹，实体加入entity文件夹，数据访问加入repository文件夹，服务写在service内与里面的impl。
2. 如有说明为“公开接口”为接口加上注解"@BypassIpWhitelist"。请求与响应都使用Json格式都需要dto且引入lombok.Getter与lombok.Setter、io.swagger.annotations内的组件增强阅读性。entity引入lombok、io.swagger.annotations、mybatisplus，数据库查询操作在serverimp内执行。
3. 如果需要引入config.properties内的变量，从config文件夹的ConfigManager.java读取。
4. 此项目为JDK25+SpringBoot4.0.1

