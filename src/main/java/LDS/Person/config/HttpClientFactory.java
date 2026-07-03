package LDS.Person.config;

import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * HttpClient工厂类 - 单例模式
 * 提供配置好的HttpClient实例，避免重复创建
 * 复用HttpClient可以提高性能并减少资源消耗
 * 支持从ConfigManager读取代理配置
 */
public class HttpClientFactory {
    
    private static volatile HttpClient instance;
    
    /**
     * 私有构造函数，防止实例化
     */
    private HttpClientFactory() {
    }
    
    /**
     * 获取共享的HttpClient实例（双重检查锁定）
     * HttpClient是线程安全的，可以在多个线程间共享使用
     * @return 配置好的HttpClient实例
     */
    public static HttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClientFactory.class) {
                if (instance == null) {
                    HttpClient.Builder builder = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(20))
                            .version(HttpClient.Version.HTTP_2); // 使用HTTP/2提高性能
                    
                    // 从ConfigManager读取代理配置
                    ConfigManager configManager = ConfigManager.getInstance();
                    
                    if (configManager.isProxyOpen()) {
                        String proxyHost = configManager.getProxyHost();
                        int proxyPort = configManager.getProxyPort();
                        
                        try {
                            java.net.ProxySelector proxySelector = java.net.ProxySelector.of(
                                new InetSocketAddress(proxyHost, proxyPort)
                            );
                            builder.proxy(proxySelector);
                            System.out.println("[HttpClientFactory] 已配置HTTP代理: " + proxyHost + ":" + proxyPort);
                        } catch (Exception e) {
                            System.err.println("[HttpClientFactory] 代理配置失败: " + e.getMessage());
                        }
                    } else {
                        System.out.println("[HttpClientFactory] 代理未启用，使用直连模式");
                    }
                    
                    instance = builder.build();
                    System.out.println("[HttpClientFactory] HttpClient实例已创建");
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取自定义超时时间的HttpClient实例
     * 注意：此方法会创建新的HttpClient实例，不使用缓存
     * 建议仅在确实需要不同超时设置时使用，否则应使用getInstance()
     * 
     * @param timeoutSeconds 超时时间（秒）
     * @return 配置好的HttpClient实例
     */
    public static HttpClient getInstanceWithTimeout(int timeoutSeconds) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .version(HttpClient.Version.HTTP_2);
        
        // 从ConfigManager读取代理配置
        ConfigManager configManager = ConfigManager.getInstance();
        
        if (configManager.isProxyOpen()) {
            String proxyHost = configManager.getProxyHost();
            int proxyPort = configManager.getProxyPort();
            
            try {
                java.net.ProxySelector proxySelector = java.net.ProxySelector.of(
                    new InetSocketAddress(proxyHost, proxyPort)
                );
                builder.proxy(proxySelector);
            } catch (Exception e) {
                System.err.println("[HttpClientFactory] 代理配置失败: " + e.getMessage());
            }
        }
        
        return builder.build();
    }
}
