package LDS.Person.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.PreparedStatement;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 合并的 API 日志过滤器：
 * - 包装请求与响应以缓存内容
 * - 记录单一格式的完整日志
 */
@Component
public class ApiLogFilter extends OncePerRequestFilter {

    // 日志记录器，用于输出日志信息
    private static final Logger logger = LoggerFactory.getLogger(ApiLogFilter.class);
    // JSON 对象映射器，用于序列化和反序列化 JSON 数据
    private static final ObjectMapper mapper = new ObjectMapper();
    // 日期时间格式化器，用于格式化日志时间戳
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    // 请求/响应体的最大长度限制，超过此长度将被截断
    private static final int MAX_BODY_LENGTH = 2000;
    // 请求缓存大小限制，用于 ContentCachingRequestWrapper
    private static final int REQUEST_CACHE_LIMIT = 1024 * 1024; // 1MB

    static {
        // 配置 ObjectMapper 启用缩进输出，便于日志可读性
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    // JDBC 模板，用于执行数据库操作
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数，注入 JdbcTemplate 依赖
     * @param jdbcTemplate JDBC 模板实例
     */
    @Autowired
    public ApiLogFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 执行过滤器逻辑，记录 API 请求和响应的详细信息
     * 仅对以 "/GHapi" 开头的请求进行日志记录
     * @param request HTTP 请求对象
     * @param response HTTP 响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 检查请求 URI 是否以 "/GHapi" 开头，如果不是则直接放行
        if (request.getRequestURI() == null || !request.getRequestURI().startsWith("/GHapi")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 包装请求和响应以缓存内容，便于后续读取
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // 记录请求开始时间
        long start = System.nanoTime();
        Throwable exception = null;
        try {
            // 执行过滤器链
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Throwable t) {
            // 捕获异常，用于后续日志记录
            exception = t;
            throw t;
        } finally {
            // 计算请求处理耗时（毫秒）
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            // 初始化日志数据映射
            Map<String, Object> logData = new HashMap<>();
            logData.put("logged_at", LocalDateTime.now().format(formatter));
            logData.put("method", wrappedRequest.getMethod());
            logData.put("path", wrappedRequest.getRequestURI());
            logData.put("duration_ms", durationMs);

            // 根据响应状态码判断请求状态
            int status = wrappedResponse.getStatus();
            logData.put("status", status < 400 ? "success" : "error");
            logData.put("status_code", status);

            // 读取请求体内容
            try {
                byte[] reqBuf = wrappedRequest.getContentAsByteArray();
                if (reqBuf != null && reqBuf.length > 0) {
                    // 获取请求字符编码，默认使用 UTF-8 以避免中文乱码
                    String charsetName = wrappedRequest.getCharacterEncoding();
                    java.nio.charset.Charset charset = (charsetName == null || charsetName.equalsIgnoreCase("ISO-8859-1")) 
                        ? StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(charsetName);
                    String payload = new String(reqBuf, charset);
                    if (!payload.isBlank()) {
                        // 如果是 JSON 内容类型，尝试解析为对象，否则直接记录字符串
                        if (wrappedRequest.getContentType() != null && wrappedRequest.getContentType().contains("application/json")) {
                            try {
                                Object parsed = mapper.readValue(payload, Object.class);
                                logData.put("request_body", parsed);
                            } catch (Exception e) {
                                // 解析失败时记录原始字符串
                                logData.put("request_body", truncate(payload));
                            }
                        } else {
                            logData.put("request_body", truncate(payload));
                        }
                    }
                }
            } catch (Exception ignored) {
                // 忽略读取请求体时的异常
            }

            // 读取响应体内容（尝试解析为 JSON）
            try {
                byte[] respBuf = wrappedResponse.getContentAsByteArray();
                if (respBuf != null && respBuf.length > 0) {
                    // 获取响应字符编码，默认使用 UTF-8 以避免中文乱码
                    String charsetName = wrappedResponse.getCharacterEncoding();
                    java.nio.charset.Charset charset = (charsetName == null || charsetName.equalsIgnoreCase("ISO-8859-1")) 
                        ? StandardCharsets.UTF_8 : java.nio.charset.Charset.forName(charsetName);
                    String payload = new String(respBuf, charset);
                    if (!payload.isBlank()) {
                        // 如果是 JSON 内容类型，尝试解析为对象，否则直接记录字符串
                        if (wrappedResponse.getContentType() != null && wrappedResponse.getContentType().contains("application/json")) {
                            try {
                                Object parsed = mapper.readValue(payload, Object.class);
                                logData.put("response", parsed);
                            } catch (Exception e) {
                                // 解析失败时记录原始字符串
                                logData.put("response", truncate(payload));
                            }
                        } else {
                            logData.put("response", truncate(payload));
                        }
                    }
                }
            } catch (Exception ignored) {
                // 忽略读取响应体时的异常
            }

            // 如果发生异常，记录异常信息
            if (exception != null) {
                Map<String, Object> err = new HashMap<>();
                err.put("exception_type", exception.getClass().getSimpleName());
                err.put("message", exception.getMessage());
                logData.put("error", err);
            }

            // 输出日志到控制台和数据库
            try {
                // 将日志数据序列化为 JSON 字符串
                String jsonLog = mapper.writeValueAsString(logData);
                if (logger.isInfoEnabled()) {
                    // 输出格式化的日志到控制台
                    logger.info("\n========== API LOG ==========\n{}\n========== END LOG ==========", jsonLog);
                }

                // 插入日志到数据库的 api_raw_logs 表
                try {
                    String sqlRaw = "INSERT INTO api_raw_logs(raw_json) VALUES (?)";
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(sqlRaw);
                        PGobject jsonObj = new PGobject();
                        jsonObj.setType("jsonb");
                        jsonObj.setValue(jsonLog);
                        ps.setObject(1, jsonObj);
                        return ps;
                    });

                } catch (Exception dbEx) {
                    // 记录数据库插入失败的警告
                    logger.warn("Failed to save action log to database", dbEx);
                }

            } catch (Exception e) {
                // 记录序列化日志数据失败的警告
                logger.warn("Failed to serialize log data", e);
            }

            // 必须将响应体复制回原始响应，以发送给客户端
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * 截断字符串到最大长度，如果超过则添加省略号
     * @param s 要截断的字符串
     * @return 截断后的字符串
     */
    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= MAX_BODY_LENGTH) return s;
        return s.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
    }
}
