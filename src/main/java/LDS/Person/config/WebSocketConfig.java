package LDS.Person.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;

/**
 * WebSocket配置类
 * 用于配置WebSocket端点和拦截器
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  @Value("${websocket.console.enabled:true}")
  private boolean enabled;

  @Value("${websocket.console.endpoint:/ws/console}")
  private String endpoint;

  @Value("${websocket.console.password:}")
  private String password;

  private final ConsoleWebSocketHandler consoleWebSocketHandler;

  public WebSocketConfig(ConsoleWebSocketHandler consoleWebSocketHandler) {
    this.consoleWebSocketHandler = consoleWebSocketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    if (enabled) {
      registry.addHandler(consoleWebSocketHandler, endpoint)
          .addInterceptors(passwordHandshakeInterceptor())
          .setAllowedOrigins("*"); // 允许所有来源，生产环境建议配置具体域名
    }
  }

  /**
   * 密码握手拦截器
   * 在WebSocket握手阶段验证密码
   */
  @Bean
  public HandshakeInterceptor passwordHandshakeInterceptor() {
    return new HandshakeInterceptor() {
      @Override
      public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
          WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 如果没有配置密码，则不进行验证
        if (password == null || password.isEmpty()) {
          return true;
        }

        // 从查询参数中获取密码
        String query = request.getURI().getQuery();
        if (query != null) {
          String[] params = query.split("&");
          for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "password".equals(keyValue[0])) {
              if (password.equals(keyValue[1])) {
                return true;
              }
            }
          }
        }

        // 密码验证失败
        response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return false;
      }

      @Override
      public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
          WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理，这里不需要做什么
      }
    };
  }
}
