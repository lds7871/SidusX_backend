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
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.sql.PreparedStatement;
import org.postgresql.util.PGobject;

/**
 * 合并的 API 日志过滤器：
 * - 包装请求与响应以缓存内容
 * - 记录单一格式的完整日志
 */
@Component
public class ApiLogFilter extends OncePerRequestFilter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_BODY_LENGTH = 2000;
    // Content cache size used when wrapping requests so the ContentCachingRequestWrapper constructor
    // with a cache limit is used (some Spring versions require the two-arg constructor)
    private static final int REQUEST_CACHE_LIMIT = 1024 * 1024; // 1MB

    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ApiLogFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (request.getRequestURI() == null || !request.getRequestURI().startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, REQUEST_CACHE_LIMIT);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long start = System.nanoTime();
        Throwable exception = null;
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } catch (Throwable t) {
            exception = t;
            throw t;
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000;

            Map<String, Object> logData = new HashMap<>();
            logData.put("logged_at", LocalDateTime.now().format(formatter));
            logData.put("method", wrappedRequest.getMethod());
            logData.put("path", wrappedRequest.getRequestURI());
            logData.put("duration_ms", durationMs);

            int status = wrappedResponse.getStatus();
            logData.put("status", status < 400 ? "success" : "error");
            logData.put("status_code", status);

            // read request body (simple charset decoding)
            try {
                byte[] reqBuf = wrappedRequest.getContentAsByteArray();
                if (reqBuf != null && reqBuf.length > 0) {
                    String charset = wrappedRequest.getCharacterEncoding() != null ? wrappedRequest.getCharacterEncoding() : "UTF-8";
                    String payload = new String(reqBuf, Charset.forName(charset));
                    if (!payload.isBlank()) {
                        if (wrappedRequest.getContentType() != null && wrappedRequest.getContentType().contains("application/json")) {
                            try {
                                Object parsed = mapper.readValue(payload, Object.class);
                                logData.put("request_body", parsed);
                            } catch (Exception e) {
                                logData.put("request_body", truncate(payload));
                            }
                        } else {
                            logData.put("request_body", truncate(payload));
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            // 读取响应体（尝试解析为 JSON）
            try {
                byte[] respBuf = wrappedResponse.getContentAsByteArray();
                if (respBuf != null && respBuf.length > 0) {
                    String charset = wrappedResponse.getCharacterEncoding() != null ? wrappedResponse.getCharacterEncoding() : "UTF-8";
                    String payload = new String(respBuf, Charset.forName(charset));
                    if (!payload.isBlank()) {
                        if (wrappedResponse.getContentType() != null && wrappedResponse.getContentType().contains("application/json")) {
                            try {
                                Object parsed = mapper.readValue(payload, Object.class);
                                logData.put("response", parsed);
                            } catch (Exception e) {
                                logData.put("response", truncate(payload));
                            }
                        } else {
                            logData.put("response", truncate(payload));
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            if (exception != null) {
                Map<String, Object> err = new HashMap<>();
                err.put("exception_type", exception.getClass().getSimpleName());
                err.put("message", exception.getMessage());
                logData.put("error", err);
            }

            // 输出到终端
            try {
                String jsonLog = mapper.writeValueAsString(logData);
                System.out.println("\n========== API LOG ==========");
                System.out.println(jsonLog);
                System.out.println("========== END LOG ==========");

                // 插入到 api_raw_logs 表（存为 JSONB）
                try {
                    String sql = "INSERT INTO api_raw_logs(raw_json) VALUES (?)";
                    jdbcTemplate.update(connection -> {
                        PreparedStatement ps = connection.prepareStatement(sql);
                        PGobject jsonObj = new PGobject();
                        jsonObj.setType("jsonb");
                        jsonObj.setValue(jsonLog);
                        ps.setObject(1, jsonObj);
                        return ps;
                    });
                } catch (Exception dbEx) {
                    logger.warn("Failed to save action log to database", dbEx);
                }

            } catch (Exception e) {
                logger.warn("Failed to serialize log data", e);
            }

            // 必须把响应体复制回原始响应以发送给客户端
            wrappedResponse.copyBodyToResponse();
        }
    }

    private String truncate(String s) {
        if (s == null) return null;
        if (s.length() <= MAX_BODY_LENGTH) return s;
        return s.substring(0, MAX_BODY_LENGTH) + "...(truncated)";
    }
}
