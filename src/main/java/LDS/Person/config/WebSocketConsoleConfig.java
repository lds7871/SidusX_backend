package LDS.Person.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * WebSocket控制台配置类
 * 用于配置WebSocket端点、密码验证和控制台输出重定向
 */
@Configuration
@EnableWebSocket
public class WebSocketConsoleConfig implements WebSocketConfigurer {

  private static final Logger logger = LoggerFactory.getLogger(WebSocketConsoleConfig.class);

  @Value("${websocket.console.enabled:true}")
  private boolean enabled;

  @Value("${websocket.console.endpoint:/ws/console}")
  private String endpoint;

  @Value("${websocket.console.password:}")
  private String password;

  private final WebSocketConsoleHandler consoleHandler;

  private PrintStream originalOut;
  private PrintStream originalErr;

  public WebSocketConsoleConfig(WebSocketConsoleHandler consoleHandler) {
    this.consoleHandler = consoleHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    if (enabled) {
      registry.addHandler(consoleHandler, endpoint)
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

  /**
   * 应用启动完成后激活控制台重定向
   */
  @EventListener(ApplicationReadyEvent.class)
  public void redirectConsoleOutput() {
    if (!enabled) {
      logger.info("WebSocket 控制台输出已禁用");
      return;
    }

    // 保存原始的输出流
    originalOut = System.out;
    originalErr = System.err;

    // 创建新的输出流，同时输出到原始控制台和 WebSocket
    System.setOut(
        new PrintStream(new TeeOutputStream(originalOut, consoleHandler, false), true, Charset.forName("GBK")));
    System.setErr(
        new PrintStream(new TeeOutputStream(originalErr, consoleHandler, true), true, Charset.forName("GBK")));

    logger.info("控制台输出重定向已激活 - 所有输出将通过 WebSocket 广播");
  }

  /**
   * Tee 输出流 - 同时写入原始流和 WebSocket
   */
  private static class TeeOutputStream extends OutputStream {
    private final PrintStream originalStream;
    private final WebSocketConsoleHandler webSocketHandler;
    private final boolean isError;
    private final StringBuilder lineBuffer = new StringBuilder();

    public TeeOutputStream(PrintStream originalStream, WebSocketConsoleHandler webSocketHandler, boolean isError) {
      this.originalStream = originalStream;
      this.webSocketHandler = webSocketHandler;
      this.isError = isError;
    }

    @Override
    public void write(int b) {
      // 写入原始流
      originalStream.write(b);

      // 收集完整行后再发送到 WebSocket
      char c = (char) b;
      lineBuffer.append(c);

      // 遇到换行符时发送
      if (c == '\n') {
        String line = lineBuffer.toString();
        if (webSocketHandler != null) {
          webSocketHandler.broadcast(line);
        }
        lineBuffer.setLength(0);
      }
    }

    @Override
    public void write(byte[] b, int off, int len) {
      // 写入原始流
      originalStream.write(b, off, len);

      // 转换为字符串并发送
      String text = new String(b, off, len, Charset.forName("GBK"));
      lineBuffer.append(text);

      // 检查是否包含换行符
      int lastNewline = lineBuffer.lastIndexOf("\n");
      if (lastNewline >= 0) {
        String toSend = lineBuffer.substring(0, lastNewline + 1);
        if (webSocketHandler != null) {
          webSocketHandler.broadcast(toSend);
        }
        lineBuffer.delete(0, lastNewline + 1);
      }
    }

    @Override
    public void flush() {
      originalStream.flush();
      // 刷新时如果有未发送的内容，立即发送
      if (lineBuffer.length() > 0) {
        String remaining = lineBuffer.toString();
        if (webSocketHandler != null) {
          webSocketHandler.broadcast(remaining);
        }
        lineBuffer.setLength(0);
      }
    }
  }
}
