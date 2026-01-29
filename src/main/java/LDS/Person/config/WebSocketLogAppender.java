package LDS.Person.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * WebSocket日志Appender
 * 将日志输出通过WebSocket发送到客户端
 * 
 * 这个Appender需要在logback-spring.xml中配置
 */
@Component
public class WebSocketLogAppender extends AppenderBase<ILoggingEvent> implements ApplicationContextAware {

  private static ApplicationContext applicationContext;
  private ConsoleWebSocketHandler webSocketHandler;

  @Override
  public void setApplicationContext(ApplicationContext context) {
    applicationContext = context;
  }

  @Override
  public void start() {
    super.start();
    // 延迟获取WebSocketHandler，避免循环依赖
  }

  @Override
  protected void append(ILoggingEvent event) {
    // 延迟初始化WebSocketHandler
    if (webSocketHandler == null && applicationContext != null) {
      try {
        webSocketHandler = applicationContext.getBean(ConsoleWebSocketHandler.class);
      } catch (Exception e) {
        // 应用启动时可能还没有初始化完成，忽略异常
        return;
      }
    }

    if (webSocketHandler != null) {
      // 格式化日志消息
      String formattedMessage = formatLogEvent(event);
      // 广播到所有WebSocket客户端
      webSocketHandler.broadcast(formattedMessage);
    }
  }

  /**
   * 格式化日志事件为可读的字符串
   */
  private String formatLogEvent(ILoggingEvent event) {
    StringBuilder sb = new StringBuilder();

    // 时间戳
    sb.append("[").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        .format(new java.util.Date(event.getTimeStamp()))).append("] ");

    // 日志级别
    sb.append("[").append(event.getLevel()).append("] ");

    // 线程名
    sb.append("[").append(event.getThreadName()).append("] ");

    // Logger名称（简化包名）
    String loggerName = event.getLoggerName();
    if (loggerName.length() > 50) {
      int lastDot = loggerName.lastIndexOf('.');
      if (lastDot > 0) {
        loggerName = "..." + loggerName.substring(lastDot);
      }
    }
    sb.append(loggerName).append(" - ");

    // 日志消息
    sb.append(event.getFormattedMessage());

    // 异常信息
    if (event.getThrowableProxy() != null) {
      sb.append("\n").append(event.getThrowableProxy().getMessage());
    }

    sb.append("\n");

    return sb.toString();
  }
}
