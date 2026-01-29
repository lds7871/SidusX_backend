package LDS.Person.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * 控制台WebSocket处理器
 * 负责管理WebSocket会话和消息广播
 */
@Component
public class ConsoleWebSocketHandler extends TextWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(ConsoleWebSocketHandler.class);

  // 活动的WebSocket会话集合
  private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  // 消息队列，用于异步发送消息
  private final LinkedBlockingQueue<String> messageQueue;

  // 历史消息缓存，新连接时可以获取
  private final List<String> historyBuffer;

  @Value("${websocket.console.max-text-message-buffer-size:512000}")
  private int maxTextMessageBufferSize;

  @Value("${websocket.console.max-binary-message-buffer-size:512000}")
  private int maxBinaryMessageBufferSize;

  @Value("${websocket.console.max-session-idle-timeout:300000}")
  private long maxSessionIdleTimeout;

  @Value("${websocket.console.message-queue-size:1000}")
  private int messageQueueSize;

  @Value("${websocket.console.history-buffer-size:100}")
  private int historyBufferSize;

  public ConsoleWebSocketHandler() {
    this.messageQueue = new LinkedBlockingQueue<>();
    this.historyBuffer = new CopyOnWriteArrayList<>();
  }

  @PostConstruct
  public void init() {
    // 启动消息发送线程
    Thread senderThread = new Thread(this::processMessageQueue, "WebSocket-Message-Sender");
    senderThread.setDaemon(true);
    senderThread.start();
    logger.info("WebSocket Console Handler 已初始化");
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // 配置会话参数
    session.setTextMessageSizeLimit(maxTextMessageBufferSize);
    session.setBinaryMessageSizeLimit(maxBinaryMessageBufferSize);

    // 添加到活动会话
    sessions.put(session.getId(), session);
    logger.info("WebSocket 连接建立: {} (总连接数: {})", session.getId(), sessions.size());

    // 发送欢迎消息
    sendToSession(session, "=== 欢迎使用控制台WebSocket ===\n");
    sendToSession(session, "连接ID: " + session.getId() + "\n");
    sendToSession(session, "当前时间: " + new java.util.Date() + "\n");
    sendToSession(session, "================================\n\n");

    // 发送历史消息
    if (!historyBuffer.isEmpty()) {
      sendToSession(session, "=== 最近的日志记录 ===\n");
      for (String msg : historyBuffer) {
        sendToSession(session, msg);
      }
      sendToSession(session, "=== 开始实时日志 ===\n\n");
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    sessions.remove(session.getId());
    logger.info("WebSocket 连接关闭: {} (总连接数: {})", session.getId(), sessions.size());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    logger.error("WebSocket 传输错误: {}", session.getId(), exception);
    sessions.remove(session.getId());
  }

  /**
   * 广播消息到所有连接的客户端
   * 将消息添加到队列中异步发送
   */
  public void broadcast(String message) {
    if (message == null || message.isEmpty()) {
      return;
    }

    // 添加到历史缓存
    addToHistory(message);

    // 如果没有活动会话，不处理消息
    if (sessions.isEmpty()) {
      return;
    }

    // 添加到消息队列
    if (!messageQueue.offer(message)) {
      logger.warn("消息队列已满，丢弃消息");
    }
  }

  /**
   * 添加消息到历史缓存
   */
  private void addToHistory(String message) {
    historyBuffer.add(message);
    // 保持缓存大小
    while (historyBuffer.size() > historyBufferSize) {
      historyBuffer.remove(0);
    }
  }

  /**
   * 处理消息队列，异步发送消息
   */
  private void processMessageQueue() {
    while (true) {
      try {
        String message = messageQueue.take(); // 阻塞等待消息

        // 发送到所有活动会话
        for (WebSocketSession session : sessions.values()) {
          sendToSession(session, message);
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        logger.warn("消息发送线程被中断");
        break;
      } catch (Exception e) {
        logger.error("处理消息队列时发生错误", e);
      }
    }
  }

  /**
   * 发送消息到指定会话
   */
  private void sendToSession(WebSocketSession session, String message) {
    if (session.isOpen()) {
      try {
        synchronized (session) {
          session.sendMessage(new TextMessage(message));
        }
      } catch (Exception e) {
        logger.error("发送消息失败: {}", session.getId(), e);
        sessions.remove(session.getId());
        try {
          session.close();
        } catch (Exception closeEx) {
          // 忽略关闭异常
        }
      }
    }
  }

  /**
   * 获取当前活动连接数
   */
  public int getActiveConnectionCount() {
    return sessions.size();
  }
}
