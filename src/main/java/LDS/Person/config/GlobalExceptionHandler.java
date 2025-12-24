package LDS.Person.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * 用于处理未捕获的异常，并标记相关请求为失败状态
 * 同时将异常请求保存到 api_log 数据库表，确保所有异常也被记录
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @Autowired(required = false)
  private JdbcTemplate jdbcTemplate;

  /**
   * 处理所有未捕获的异常
   */
  @ExceptionHandler(Exception.class)
  public String handleException(Exception ex) {
    try {
      // 获取当前请求
      ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
      HttpServletRequest request = attributes.getRequest();

      String clientIp = getClientIp(request);
      String requestUri = request.getRequestURI();
      String method = request.getMethod();

      log.error("全局异常处理 - IP: {}, URI: {}, 异常: {}", clientIp, requestUri, ex.getMessage());

      // 标记当前请求上下文为出错状态
      // 这个信息会在/error页面处理时使用
      request.setAttribute("request_failed", true);
      request.setAttribute("original_uri", requestUri);

      // 将异常请求记录到数据库
      logExceptionToDatabase(clientIp, method, requestUri, ex);

    } catch (Exception e) {
      log.error("异常处理器自身出错: {}", e.getMessage());
    }

    // 返回error页面
    return "error";
  }

  /**
   * 获取客户端IP
   */
  private String getClientIp(HttpServletRequest request) {
    String[] ipHeaders = {
        "X-Forwarded-For",
        "X-Real-IP",
        "Proxy-Client-IP",
        "WL-Proxy-Client-IP",
        "HTTP_CLIENT_IP",
        "HTTP_X_FORWARDED_FOR"
    };

    for (String header : ipHeaders) {
      String ip = request.getHeader(header);
      if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
        if (ip.contains(",")) {
          ip = ip.split(",")[0].trim();
        }
        return ip;
      }
    }

    return request.getRemoteAddr();
  }

  /**
   * 将异常请求记录到 api_log 数据库表
   * 确保所有异常请求都被记录，便于安全审计和故障排查
   * 
   * @param ip 客户端IP
   * @param method 请求方法
   * @param uri 请求URI
   * @param ex 异常对象
   */
  private void logExceptionToDatabase(String ip, String method, String uri, Exception ex) {
    if (jdbcTemplate == null) {
      log.debug("JdbcTemplate未注入，跳过数据库日志记录");
      return;
    }

    try {
      // 构建API详情字符串
      String exceptionType = ex.getClass().getSimpleName();
      String exceptionMsg = ex.getMessage() != null ? ex.getMessage() : "Unknown error";
      
      // 限制异常信息长度，避免记录过长的数据
      if (exceptionMsg.length() > 500) {
        exceptionMsg = exceptionMsg.substring(0, 500) + "...[TRUNCATED]";
      }
      
      String detailedApi = method + " " + uri + " | Exception: " + exceptionType + " - " + exceptionMsg;

      // 插入数据库记录，状态为0（失败）
      jdbcTemplate.update(connection -> {
        var ps = connection.prepareStatement(
            "INSERT INTO api_log (ip, api, states, create_time) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
            new String[] { "id" });
        ps.setString(1, ip);
        ps.setString(2, detailedApi);
        ps.setInt(3, 0); // 状态为0表示失败/异常
        return ps;
      });

      log.debug("异常请求已记录到数据库 - IP: {}, 方法: {}, URI: {}", ip, method, uri);

    } catch (Exception dbEx) {
      log.error("保存异常日志到数据库失败 - IP: {}, URI: {}", ip, uri, dbEx);
    }
  }
}