package LDS.Person.tasks;

import LDS.Person.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nginx 访问日志汇总定时任务
 * 每天早上 9:00 读取前一天的 Nginx access 日志，
 * 统计各 IP 请求次数及 HTTP 方法分布，生成汇总图片并发送邮件。
 */
@Component
public class NginxCollectionTask {

    private static final Logger logger = LoggerFactory.getLogger(NginxCollectionTask.class);

    @Autowired(required = false)
    private EmailService emailService;

    private static final String LOG_DIR    = "C:\\Users\\Administrator\\Desktop\\ServerSync\\Nginx日志\\";
    private static final String TARGET_EMAIL = "1964960588@qq.com";

    // ─── 定时任务：每天早上 9:00 执行 ─────────────────────────────────────────
    @Scheduled(cron = "0 0 9 * * ?")
    public void collectNginxLogs() {
        logger.info("===== 开始执行 Nginx 日志汇总定时任务 =====");
        try {
            String dateStr = LocalDate.now().minusDays(1)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String logFilePath = LOG_DIR + "access(" + dateStr + ").log";
            String outputPath  = System.getProperty("user.dir") + File.separator + "代理记录图片.png";

            runTask(dateStr, logFilePath, outputPath);
            logger.info("Nginx 日志汇总图片已生成: {}", outputPath);

            if (emailService != null) {
                sendReport(dateStr, outputPath);
            } else {
                logger.warn("EmailService 未注入，跳过邮件发送");
            }
            logger.info("===== Nginx 日志汇总定时任务执行完成 =====");
        } catch (Exception e) {
            logger.error("Nginx 日志汇总任务执行失败: {}", e.getMessage(), e);
        }
    }

    // ─── 邮件发送 ──────────────────────────────────────────────────────────────
    private void sendReport(String dateStr, String imagePath) {
        try {
            String subject = "Nginx 访问日志汇总报告 - " + dateStr;
            String html = "<h2>Nginx 访问日志汇总报告</h2>"
                    + "<p>统计日期：<b>" + dateStr + "</b></p>"
                    + "<p>请查看附件中的汇总统计图片。</p>";
            Map<String, String> attachments = new LinkedHashMap<>();
            attachments.put("代理记录图片.png", imagePath);
            emailService.sendMailWithAttachment(TARGET_EMAIL, subject, html, attachments);
            logger.info("汇总报告邮件已发送至: {}", TARGET_EMAIL);
        } catch (Exception e) {
            logger.error("汇总报告邮件发送失败: {}", e.getMessage(), e);
        }
    }

    // ─── 核心逻辑（可独立运行）───────────────────────────────────────────────
    public static void runTask(String dateStr, String logFilePath, String outputImagePath) throws Exception {
        Logger log = LoggerFactory.getLogger(NginxCollectionTask.class);
        log.info("读取日志文件: {}", logFilePath);

        Path logPath = Paths.get(logFilePath);
        if (!Files.exists(logPath)) {
            throw new FileNotFoundException("日志文件不存在: " + logFilePath);
        }

        Map<String, Integer> ipCounts     = new HashMap<>();
        Map<String, Integer> methodCounts = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(logPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    parseLine(line, ipCounts, methodCounts);
                }
            }
        }

        int totalRequests = ipCounts.values().stream().mapToInt(Integer::intValue).sum();
        log.info("日志解析完成：总请求 {}，独立 IP {}", totalRequests, ipCounts.size());

        // Top 20 IP（按请求数降序）
        java.util.List<Map.Entry<String, Integer>> topIps = ipCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(20)
                .collect(Collectors.toList());

        // HTTP 方法（按请求数降序）
        java.util.List<Map.Entry<String, Integer>> methodList = methodCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 查询 Top IP 的地理位置（网络不可用时自动跳过）
        java.util.List<String> topIpAddrs = topIps.stream()
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        Map<String, String> ipLocations = batchQueryIpLocations(topIpAddrs);

        generateImage(dateStr, totalRequests, ipCounts.size(), topIps, methodList, ipLocations, outputImagePath);
        log.info("汇总图片已保存: {}", outputImagePath);
    }

    /** 合法的 HTTP 方法集合（含 HTTP/2 的 PRI） */
    private static final Set<String> VALID_HTTP_METHODS = Set.of(
            "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS",
            "PATCH", "CONNECT", "TRACE", "PRI"
    );

    // ─── Nginx 日志行解析 ─────────────────────────────────────────────────────
    // 标准 combined 格式:
    // $remote_addr - $remote_user [$time_local] "$request" $status $bytes "$referer" "$ua"
    private static void parseLine(String line,
                                   Map<String, Integer> ipCounts,
                                   Map<String, Integer> methodCounts) {
        int spaceIdx = line.indexOf(' ');
        if (spaceIdx < 1) return;

        String ip = line.substring(0, spaceIdx);
        ipCounts.merge(ip, 1, Integer::sum);

        // 请求字段位于第一对双引号之间
        int q1 = line.indexOf('"');
        if (q1 < 0) return;
        int q2 = line.indexOf('"', q1 + 1);
        if (q2 < 0) return;

        String request   = line.substring(q1 + 1, q2);
        int    methodEnd = request.indexOf(' ');
        String rawMethod = methodEnd > 0 ? request.substring(0, methodEnd) : request;

        if (rawMethod.isBlank() || rawMethod.equals("-")) return;

        // 仅统计纯大写字母且长度 ≤ 10 的合法方法；其余（二进制/攻击）归为 OTHER
        String method;
        if (rawMethod.length() <= 10
                && rawMethod.chars().allMatch(c -> c >= 'A' && c <= 'Z')
                && VALID_HTTP_METHODS.contains(rawMethod)) {
            method = rawMethod;
        } else {
            method = "OTHER";
        }
        methodCounts.merge(method, 1, Integer::sum);
    }

    // ─── IP 地理位置查询（批量，使用 ip-api.com 免费接口）────────────────────
    private static Map<String, String> batchQueryIpLocations(java.util.List<String> ips) {
        Logger log = LoggerFactory.getLogger(NginxCollectionTask.class);
        Map<String, String> result = new LinkedHashMap<>();
        java.util.List<String> publicIps = new ArrayList<>();

        for (String ip : ips) {
            if (isLoopbackIp(ip)) {
                result.put(ip, "本地回环");
            } else if (isPrivateIp(ip)) {
                result.put(ip, "内网地址");
            } else {
                publicIps.add(ip);
            }
        }

        if (publicIps.isEmpty()) return result;

        try {
            // 构建 IP 字符串数组 body
            StringBuilder jsonBody = new StringBuilder("[");
            for (int i = 0; i < publicIps.size(); i++) {
                if (i > 0) jsonBody.append(",");
                jsonBody.append("\"").append(publicIps.get(i)).append("\"");
            }
            jsonBody.append("]");

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(6))
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://ip-api.com/batch?fields=query,country,regionName,city&lang=zh-CN"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(12))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JSONArray arr = JSON.parseArray(response.body());
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    String ip      = obj.getString("query");
                    String country = obj.getString("country");
                    String region  = obj.getString("regionName");
                    String city    = obj.getString("city");
                    result.put(ip, buildLocationStr(country, region, city));
                }
            } else {
                log.warn("ip-api.com 返回非 200 状态: {}", response.statusCode());
                publicIps.forEach(ip -> result.putIfAbsent(ip, ""));
            }
        } catch (Exception e) {
            log.warn("IP 地理位置查询失败（将跳过显示）: {}", e.getMessage());
            publicIps.forEach(ip -> result.putIfAbsent(ip, ""));
        }
        return result;
    }

    private static String buildLocationStr(String country, String region, String city) {
        StringBuilder sb = new StringBuilder();
        if (country != null && !country.isBlank()) sb.append(country);
        if (region != null && !region.isBlank() && !region.equals(country)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(region);
        }
        if (city != null && !city.isBlank() && !city.equals(region) && !city.equals(country)) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(city);
        }
        String loc = sb.toString();
        // 截断超长地址，防止溢出图片布局
        return loc.length() > 14 ? loc.substring(0, 13) + "\u2026" : loc;
    }

    private static boolean isLoopbackIp(String ip) {
        return ip.startsWith("127.") || ip.equals("::1")
                || ip.equalsIgnoreCase("localhost")
                || ip.equals("0:0:0:0:0:0:0:1");
    }

    private static boolean isPrivateIp(String ip) {
        if (isLoopbackIp(ip)) return true;
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) return true;
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                return second >= 16 && second <= 31;
            } catch (Exception ignored) {}
        }
        return false;
    }

    // ─── 图片生成 ─────────────────────────────────────────────────────────────
    private static void generateImage(String dateStr, int totalRequests, int uniqueIps,
                                       java.util.List<Map.Entry<String, Integer>> topIps,
                                       java.util.List<Map.Entry<String, Integer>> methods,
                                       Map<String, String> ipLocations,
                                       String outputPath) throws IOException {
        System.setProperty("java.awt.headless", "true");

        final int W          = 1060;
        final int HEADER_H   = 124;
        final int SECTION_H  = 42;
        final int ROW_H      = 34;
        final int PADDING    = 22;
        final int BAR_MAX_W  = 400;

        int H = HEADER_H
                + SECTION_H + methods.size() * ROW_H + PADDING
                + SECTION_H + topIps.size() * ROW_H + PADDING
                + 30;

        // ── 颜色主题（深色科技风）──
        Color bgColor       = new Color(0x1a1a2e);
        Color headerBg      = new Color(0x16213e);
        Color sectionBg     = new Color(0x0f3460);
        Color rowBg         = new Color(0x1e2245);
        Color rowBgAlt      = new Color(0x16213e);
        Color accentRed     = new Color(0xe94560);
        Color accentCyan    = new Color(0x53d8fb);
        Color accentGreen   = new Color(0x98fb98);
        Color accentYellow  = new Color(0xffd700);
        Color textGray      = new Color(0xaaaacc);
        Color dividerColor  = new Color(0x0f3460);

        BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D    g     = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        // ── 全局背景 ──
        g.setColor(bgColor);
        g.fillRect(0, 0, W, H);

        // ── 字体 ──
        Font cjkLarge = pickFont(Font.BOLD, 22);
        Font cjkMed   = pickFont(Font.BOLD, 15);
        Font cjkSmall = pickFont(Font.PLAIN, 13);
        Font mono     = new Font(Font.MONOSPACED, Font.PLAIN, 14);

        // ── Header ──
        g.setColor(headerBg);
        g.fillRect(0, 0, W, HEADER_H);

        g.setFont(cjkLarge);
        g.setColor(accentRed);
        g.drawString("Nginx 访问日志汇总报告", PADDING, 46);

        g.setFont(cjkMed);
        g.setColor(textGray);
        g.drawString("统计日期：" + dateStr, PADDING, 76);
        g.drawString("总请求数：" + totalRequests + "    独立 IP 数：" + uniqueIps, PADDING, 104);

        // Header 底部分隔线
        g.setColor(accentRed);
        g.fillRect(0, HEADER_H - 2, W, 2);

        // ── 请求方式统计 ──
        int y = HEADER_H;

        g.setColor(sectionBg);
        g.fillRect(0, y, W, SECTION_H);
        g.setFont(cjkMed);
        g.setColor(accentCyan);
        g.drawString("  请求方式统计", PADDING, y + 27);
        y += SECTION_H;

        int maxMethodCnt = methods.isEmpty() ? 1 : methods.get(0).getValue();
        for (int i = 0; i < methods.size(); i++) {
            Map.Entry<String, Integer> e = methods.get(i);
            g.setColor(i % 2 == 0 ? rowBg : rowBgAlt);
            g.fillRect(0, y, W, ROW_H);
            g.setColor(dividerColor);
            g.fillRect(0, y + ROW_H - 1, W, 1);

            int barW = maxMethodCnt == 0 ? 2 : Math.max(2, (int) ((double) e.getValue() / maxMethodCnt * BAR_MAX_W));

            g.setFont(mono);
            g.setColor(accentYellow);
            g.drawString(e.getKey(), PADDING, y + 23);

            // 百分比文字
            int pct = (int) Math.round(e.getValue() * 100.0 / Math.max(totalRequests, 1));
            g.setColor(textGray);
            g.drawString(pct + "%", PADDING + 95, y + 23);

            g.setColor(accentCyan);
            g.fillRoundRect(PADDING + 135, y + 8, barW, ROW_H - 16, 5, 5);

            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(e.getValue()), PADDING + 135 + barW + 10, y + 23);

            y += ROW_H;
        }

        y += PADDING;

        // ── TOP IP 访问统计 ──
        g.setColor(sectionBg);
        g.fillRect(0, y, W, SECTION_H);
        g.setFont(cjkMed);
        g.setColor(accentCyan);
        g.drawString("  TOP IP 访问统计（前 " + topIps.size() + " 名）", PADDING, y + 27);
        y += SECTION_H;

        int maxIpCnt = topIps.isEmpty() ? 1 : topIps.get(0).getValue();
        for (int rank = 1; rank <= topIps.size(); rank++) {
            Map.Entry<String, Integer> e = topIps.get(rank - 1);
            g.setColor(rank % 2 == 0 ? rowBg : rowBgAlt);
            g.fillRect(0, y, W, ROW_H);
            g.setColor(dividerColor);
            g.fillRect(0, y + ROW_H - 1, W, 1);

            int ipBarW = maxIpCnt == 0 ? 2 : Math.max(2, (int) ((double) e.getValue() / maxIpCnt * 380));

            g.setFont(mono);
            // 排名（前3名用红色突出）
            g.setColor(rank <= 3 ? accentRed : textGray);
            g.drawString(String.format("%2d.", rank), PADDING, y + 23);

            // IP 地址
            g.setColor(accentGreen);
            g.drawString(String.format("%-17s", e.getKey()), PADDING + 36, y + 23);

            // 地理位置
            String location = ipLocations.getOrDefault(e.getKey(), "");
            if (!location.isEmpty()) {
                g.setFont(cjkSmall);
                g.setColor(textGray);
                g.drawString(location, PADDING + 185, y + 23);
            }

            // 柱状条
            g.setColor(rank <= 3 ? accentRed : accentCyan);
            g.fillRoundRect(PADDING + 398, y + 8, ipBarW, ROW_H - 16, 5, 5);

            // 请求数
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf(e.getValue()), PADDING + 398 + ipBarW + 10, y + 23);

            y += ROW_H;
        }

        g.dispose();

        File outFile = new File(outputPath);
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }
        if (!ImageIO.write(image, "PNG", outFile)) {
            throw new IOException("ImageIO 未找到合适的 PNG 写入器，无法保存图片");
        }
    }

    /** 优先选取支持中文的字体，回退至系统无衬线字体 */
    private static Font pickFont(int style, int size) {
        String[] preferredCjk = {
            "Microsoft YaHei", "微软雅黑", "SimHei", "黑体", "SimSun", "宋体"
        };
        try {
            Set<String> available = new HashSet<>(Arrays.asList(
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
            for (String name : preferredCjk) {
                if (available.contains(name)) {
                    return new Font(name, style, size);
                }
            }
        } catch (Exception ignored) {}
        return new Font(Font.SANS_SERIF, style, size);
    }

    // ─── 调试 / 测试入口 ──────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "true");

        String dateStr     = LocalDate.now().minusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String logFilePath = LOG_DIR + "access(" + dateStr + ").log";
        String outputPath  = System.getProperty("user.dir") + File.separator + "代理记录图片.png";

        System.out.println("[DEBUG] 统计日期  : " + dateStr);
        System.out.println("[DEBUG] 日志文件  : " + logFilePath);
        System.out.println("[DEBUG] 输出图片  : " + outputPath);

        try {
            runTask(dateStr, logFilePath, outputPath);
            System.out.println("[DEBUG] 任务完成，图片已生成: " + outputPath);
            System.out.println("[DEBUG] 提示：main 模式下不发送邮件，如需测试邮件请在 Spring 上下文中触发定时任务");
        } catch (FileNotFoundException e) {
            System.err.println("[ERROR] 日志文件不存在，请检查路径: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] 任务执行异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
