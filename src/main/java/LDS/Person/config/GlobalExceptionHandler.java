package LDS.Person.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * 用于处理未捕获的异常，并标记相关请求为失败状态
 */
@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

      log.error("全局异常处理 - IP: {}, URI: {}, 异常: {}", clientIp, requestUri, ex.getMessage());

      // 标记当前请求上下文为出错状态
      // 这个信息会在/error页面处理时使用
      request.setAttribute("request_failed", true);
      request.setAttribute("original_uri", requestUri);

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
}