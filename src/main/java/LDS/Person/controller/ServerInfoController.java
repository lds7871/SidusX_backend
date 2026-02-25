package LDS.Person.controller;

import LDS.Person.entity.ApiLog;
import LDS.Person.dto.request.ApiLogQueryRequest;
import LDS.Person.dto.response.ApiLogResponse;
import LDS.Person.dto.response.ApiLogSimpleResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器监控控制器 - 获取当前 Spring 服务的内存和 JVM 虚拟机情况
 */
@RestController
@RequestMapping("/GHapi/serverinfo")
@Tag(name = "服务监控", description = "获取服务信息")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ServerInfoController {

    private static final Logger log = LoggerFactory.getLogger(ServerInfoController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取完整的 JVM 和系统概览
     */
    @GetMapping("/JVMoverview")
    @Operation(summary = "获取JVM信息", description = "返回 JVM 内存、系统信息、线程等所有信息的汇总")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> overviewData = new HashMap<>();

            // 内存信息
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemory = memoryMXBean.getHeapMemoryUsage();
            Map<String, Object> memSummary = new HashMap<>();
            memSummary.put("堆已使用_MB", heapMemory.getUsed() / 1024 / 1024);
            memSummary.put("堆最大_MB", heapMemory.getMax() / 1024 / 1024);
            memSummary.put("堆使用率", String.format("%.2f%%", (double) heapMemory.getUsed() / heapMemory.getMax() * 100));
            overviewData.put("内存概览", memSummary);

            // CPU 和线程信息
            java.lang.management.OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            Map<String, Object> cpuSummary = new HashMap<>();
            cpuSummary.put("可用处理器数", osBean.getAvailableProcessors());
            cpuSummary.put("系统负载平均值", osBean.getSystemLoadAverage());
            overviewData.put("CPU概览", cpuSummary);

            // 线程信息
            Map<String, Object> threadSummary = new HashMap<>();
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            threadSummary.put("当前线程数", threadMXBean.getThreadCount());
            threadSummary.put("峰值线程数", threadMXBean.getPeakThreadCount());
            overviewData.put("线程概览", threadSummary);

            // JVM 版本
            Map<String, Object> jvmSummary = new HashMap<>();
            jvmSummary.put("Java版本", System.getProperty("java.version"));
            jvmSummary.put("JVM名称", System.getProperty("java.vm.name"));
            overviewData.put("JVM信息", jvmSummary);

            response.put("状态码", 200);
            response.put("消息", "✅ 概览信息获取成功");
            response.put("数据", overviewData);
            response.put("时间戳", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 获取概览信息失败", e);
            response.put("code", 500);
            response.put("message", "获取失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取服务启动时间与内存信息（轻量接口）
     */
    @GetMapping("/startup")
    @Operation(summary = "服务启动与内存信息", description = "返回应用启动时间、运行时长以及内存使用情况（MB）")
    public ResponseEntity<Map<String, Object>> getStartupAndMemory() {
        Map<String, Object> response = new HashMap<>();
        try {
            java.lang.management.RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            long startMillis = runtimeMXBean.getStartTime();
            long uptimeMillis = runtimeMXBean.getUptime();

            Instant startInstant = Instant.ofEpochMilli(startMillis);
            long uptimeSeconds = Duration.ofMillis(uptimeMillis).getSeconds();

            // JVM 内存（Heap / Non-heap）
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryMXBean.getNonHeapMemoryUsage();

            Runtime rt = Runtime.getRuntime();
            long runtimeTotal = rt.totalMemory();
            long runtimeFree = rt.freeMemory();
            long runtimeUsed = runtimeTotal - runtimeFree;

            Map<String, Object> data = new HashMap<>();
            data.put("启动时间", startInstant.toString());
            data.put("运行时长(秒)", uptimeSeconds);

            Map<String, Object> heapInfo = new HashMap<>();
            heapInfo.put("堆_已用_MB", heap.getUsed() / 1024 / 1024);
            heapInfo.put("堆_已提交_MB", heap.getCommitted() / 1024 / 1024);
            heapInfo.put("堆_最大_MB", heap.getMax() / 1024 / 1024);
            data.put("堆内存", heapInfo);

            Map<String, Object> nonHeapInfo = new HashMap<>();
            nonHeapInfo.put("非堆_已用_MB", nonHeap.getUsed() / 1024 / 1024);
            nonHeapInfo.put("非堆_已提交_MB", nonHeap.getCommitted() / 1024 / 1024);
            data.put("非堆内存", nonHeapInfo);

            Map<String, Object> runtimeInfo = new HashMap<>();
            runtimeInfo.put("JVM总内存_MB", runtimeTotal / 1024 / 1024);
            runtimeInfo.put("JVM空闲内存_MB", runtimeFree / 1024 / 1024);
            runtimeInfo.put("JVM已用内存_MB", runtimeUsed / 1024 / 1024);
            data.put("运行时信息", runtimeInfo);

            response.put("状态码", 200);
            response.put("消息", "服务启动时间与内存信息（已本地化）");
            response.put("数据", data);
            response.put("时间戳", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 获取启动与内存信息失败", e);
            response.put("code", 500);
            response.put("message", "获取失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 查询最近20条API访问日志
     * 支持按状态和时间范围筛选
     */
    @PostMapping("/apilog")
    @Operation(summary = "查询最近20条API日志", description = "返回最近20条API访问日志，支持按状态和时间范围筛选")
    public ResponseEntity<ApiLogSimpleResultResponse> queryApiLog(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "日志查询条件") @RequestBody ApiLogQueryRequest queryRequest) {

        try {
            // 构建查询SQL
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            sql.append("SELECT id, ip, api, states, create_time FROM api_log WHERE 1=1 ");

            // 按状态码筛选
            if (queryRequest.getStates() != null) {
                sql.append("AND states = ? ");
                params.add(queryRequest.getStates());
            }

            // 按时间范围筛选
            if (queryRequest.getStartTime() != null && !queryRequest.getStartTime().isEmpty()) {
                sql.append("AND create_time >= ? ");
                params.add(LocalDateTime.parse(queryRequest.getStartTime().replace(" ", "T")));
            }
            if (queryRequest.getEndTime() != null && !queryRequest.getEndTime().isEmpty()) {
                sql.append("AND create_time <= ? ");
                params.add(LocalDateTime.parse(queryRequest.getEndTime().replace(" ", "T")));
            }

            // 按时间倒序排列，只取最近20条
            sql.append("ORDER BY create_time DESC LIMIT 20");

            // 执行查询
            java.util.List<ApiLog> logs = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                ApiLog log = new ApiLog();
                log.setId(rs.getInt("id"));
                log.setIp(rs.getString("ip"));
                log.setApi(rs.getString("api"));
                log.setStates(rs.getInt("states"));
                java.sql.Timestamp createTime = rs.getTimestamp("create_time");
                if (createTime != null) {
                    log.setCreateTime(createTime.toLocalDateTime());
                }
                return log;
            }, params.toArray(new Object[0]));

            // 转换为响应DTO
            java.util.List<ApiLogResponse> logResponses = logs.stream()
                    .map(log -> {
                        ApiLogResponse response = new ApiLogResponse();
                        response.setId(log.getId());
                        response.setIp(log.getIp());
                        response.setApi(log.getApi());
                        response.setStates(log.getStates());
                        response.setCreateTime(log.getCreateTime());
                        return response;
                    })
                    .collect(java.util.stream.Collectors.toList());

            ApiLogSimpleResultResponse resultResponse = ApiLogSimpleResultResponse.builder()
                    .code(200)
                    .message("日志查询成功")
                    .data(logResponses)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(resultResponse);
        } catch (Exception e) {
            log.error(" 日志查询失败", e);
            ApiLogSimpleResultResponse errorResponse = ApiLogSimpleResultResponse.builder()
                    .code(500)
                    .message("查询失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取数据表统计信息
     * 返回 wiki、article 和 nasa_daily_image 三个表的总记录数
     */
    @GetMapping("/table-count")
    @Operation(summary = "获取数据表记录数", description = "返回 wiki、article 和 nasa_daily_image 三个表的总记录数")
    public ResponseEntity<Map<String, Object>> getTableCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> counts = new HashMap<>();

            // 查询 wiki 表总数
            Integer wikiCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wiki", Integer.class);
            counts.put("wiki表", wikiCount != null ? wikiCount : 0);

            // 查询 article 表总数
            Integer articleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM article", Integer.class);
            counts.put("article表", articleCount != null ? articleCount : 0);

            // 查询 nasa_daily_image 表总数
            Integer nasaDailyImageCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM nasa_daily_image",
                    Integer.class);
            counts.put("nasa_daily_image表", nasaDailyImageCount != null ? nasaDailyImageCount : 0);

            // 查询 wiki_review 表总数
            Integer wikiReviewCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wiki_review", Integer.class);
            counts.put("wiki_review表", wikiReviewCount != null ? wikiReviewCount : 0);

            // 查询 wiki_new 表总数
            Integer wikiNewCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM wiki_new", Integer.class);
            counts.put("wiki_new表", wikiNewCount != null ? wikiNewCount : 0);

            response.put("状态码", 200);
            response.put("消息", "✅ 数据表统计信息获取成功");
            response.put("数据", counts);
            response.put("时间戳", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 获取数据表统计信息失败", e);
            response.put("状态码", 500);
            response.put("消息", "获取失败: " + e.getMessage());
            response.put("时间戳", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 读取Nginx访问日志
     * 读取 C:\Users\Administrator\Desktop\ServerSync\Nginx日志\access(日期).log 文件
     */
    @GetMapping("/nginx-log")
    @Operation(summary = "读取Nginx访问日志", description = "读取Nginx访问日志文件内容，支持按行数限制")
    public ResponseEntity<Map<String, Object>> getNginxLog(
            @RequestParam(value = "limit", defaultValue = "1000") int limit) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 动态获取前一天的日期
            LocalDate currentDate = LocalDate.now().minusDays(1);
            String dateStr = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String logFilePath = "C:\\Users\\Administrator\\Desktop\\ServerSync\\Nginx日志\\access(" + dateStr + ").log";

            // 读取文件内容
            java.nio.file.Path path = Paths.get(logFilePath);

            // 检查文件是否存在
            if (!Files.exists(path)) {
                response.put("状态码", 404);
                response.put("消息", "❌ 日志文件不存在: " + logFilePath);
                response.put("时间戳", System.currentTimeMillis());
                return ResponseEntity.status(404).body(response);
            }

            // 读取所有行
            List<String> allLines = Files.readAllLines(path, StandardCharsets.UTF_8);

            // 按limit限制返回的行数
            List<String> lines = new ArrayList<>();
            int startIndex = Math.max(0, allLines.size() - limit);
            for (int i = startIndex; i < allLines.size(); i++) {
                lines.add(allLines.get(i));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("总行数", allLines.size());
            data.put("返回行数", lines.size());
            data.put("日志内容", lines);

            response.put("状态码", 200);
            response.put("消息", "✅ Nginx日志读取成功");
            response.put("数据", data);
            response.put("时间戳", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (java.io.IOException e) {
            log.error("❌ 读取Nginx日志失败", e);
            response.put("状态码", 500);
            response.put("消息", "读取文件失败: " + e.getMessage());
            response.put("时间戳", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            log.error("❌ 处理Nginx日志请求失败", e);
            response.put("状态码", 500);
            response.put("消息", "处理请求失败: " + e.getMessage());
            response.put("时间戳", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }
}
