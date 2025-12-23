---
name: New-HttpClient
description: 如果需要新建java.net.http.HttpClient有关的文件时，使用此技巧.
---

按照以下操作:

1.如果需要引入config.properties内的变量，从config文件夹的ConfigManager.java读取。
2.调用config文件夹内的HttpClientFactory.java，使用配置好的HttpClient实例，避免重复创建。
