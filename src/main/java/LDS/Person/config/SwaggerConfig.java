package LDS.Person.config;

import LDS.Person.config.ConfigManager;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * OpenAPI 配置类 (SpringDoc)
 */
@Configuration
public class SwaggerConfig {

    private static final ConfigManager CONFIG_MANAGER = ConfigManager.getInstance();

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PersonLog 个人日志系统 API")
                        .description("PersonLog 个人日志管理系统的 REST API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PersonLog 开发团队")));
    }

    /**
     * 定义 RestTemplate Bean 用于发送 HTTP 请求
     * 用于调用内部 API 等（支持可配置的代理）
     */
    @Bean
    public RestTemplate restTemplate() {
        boolean proxyIsOpen = CONFIG_MANAGER.isProxyOpen();
        String proxyHost = CONFIG_MANAGER.getProxyHost();
        int proxyPort = CONFIG_MANAGER.getProxyPort();

        try {
            // 创建 RestTemplate 的底层请求工厂
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

            // 如果启用了代理，则配置代理
            if (proxyIsOpen) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                factory.setProxy(proxy);
            }

            factory.setConnectTimeout(30000);
            factory.setReadTimeout(30000);

            // 使用 BufferingClientHttpRequestFactory 包装以支持重复读取
            ClientHttpRequestFactory bufferingFactory = new BufferingClientHttpRequestFactory(factory);

            RestTemplate tpl = new RestTemplate(bufferingFactory);
            return tpl;
        } catch (Exception e) {
            // 配置失败，返回一个默认的 RestTemplate
            return new RestTemplate();
        }
    }

}
