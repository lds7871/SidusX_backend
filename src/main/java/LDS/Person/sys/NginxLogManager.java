package LDS.Person.sys;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Nginx 日志管理工具
 * 功能：
 * 1. 复制 Nginx 的 access.log 和 error.log
 * 2. 按当前日期重命名日志文件
 * 3. 删除原始日志文件
 * 4. 执行 Nginx 重启脚本
 * 
 * 定时任务：每晚 23:59 自动执行
 */
@Component
public class NginxLogManager {

    // ============ 静态路径配置 ============
    // Nginx 日志源目录
    private static final String NGINX_LOG_SOURCE = "C:\\Users\\Administrator\\Desktop\\ServerSync\\Nginx-1.28.0\\logs";

    // Nginx 日志备份目录
    private static final String NGINX_LOG_BACKUP = "C:\\Users\\Administrator\\Desktop\\ServerSync\\Nginx日志";

    // Nginx 安装目录
    private static final String NGINX_HOME = "C:\\Users\\Administrator\\Desktop\\ServerSync\\Nginx-1.28.0";

    // Nginx 重启脚本路径
    private static final String NGINX_RESTART_CMD = NGINX_HOME + "\\重启.cmd";

    // 源日志文件名
    private static final String ACCESS_LOG = "access.log";
    private static final String ERROR_LOG = "error.log";

    // 日期格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // =====================================

    /**
     * 定时任务：每晚 23:59 执行 Nginx 日志备份和重启
     * Cron 表达式: 0 59 23 * * * (秒 分 小时 日 月 周)
     */
    @Scheduled(cron = "0 59 23 * * *")
    public void executeScheduledTask() {
        try {
            System.out.println("[INFO] Nginx 日志管理流程开始...");

            // 1. 复制和重命名日志文件
            backupLogFiles();

            // 2. 删除原始日志文件
            deleteOriginalLogs();

            // 3. 执行 Nginx 重启脚本
            restartNginx();

            System.out.println("[INFO] Nginx 日志管理流程完成！");
        } catch (Exception e) {
            System.err.println("[ERROR] 执行过程中出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 主方法：可用于手动测试
     */
    public static void main(String[] args) {
        System.out.println("[WARN] 当前配置为定时任务模式（每晚23:59执行）");
        System.out.println("[INFO] 如需立即执行，请使用应用程序的定时任务管理接口");
    }

    /**
     * 备份日志文件并重命名
     */
    private static void backupLogFiles() throws IOException {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);

        // 确保备份目录存在
        Path backupDir = Paths.get(NGINX_LOG_BACKUP);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
            System.out.println("[INFO] 备份目录不存在，已创建: " + NGINX_LOG_BACKUP);
        }

        // 备份 access.log
        Path sourceAccessLog = Paths.get(NGINX_LOG_SOURCE, ACCESS_LOG);
        Path backupAccessLog = Paths.get(NGINX_LOG_BACKUP, "access(" + currentDate + ").log");
        
        if (Files.exists(sourceAccessLog)) {
            Files.copy(sourceAccessLog, backupAccessLog, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[INFO] 已备份 access.log 为: " + backupAccessLog.getFileName());
        } else {
            System.out.println("[WARN] access.log 不存在: " + sourceAccessLog);
        }

        // 备份 error.log
        Path sourceErrorLog = Paths.get(NGINX_LOG_SOURCE, ERROR_LOG);
        Path backupErrorLog = Paths.get(NGINX_LOG_BACKUP, "error(" + currentDate + ").log");
        
        if (Files.exists(sourceErrorLog)) {
            Files.copy(sourceErrorLog, backupErrorLog, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[INFO] 已备份 error.log 为: " + backupErrorLog.getFileName());
        } else {
            System.out.println("[WARN] error.log 不存在: " + sourceErrorLog);
        }
    }

    /**
     * 删除原始日志文件
     */
    private static void deleteOriginalLogs() throws IOException {
        // 删除 access.log
        Path accessLog = Paths.get(NGINX_LOG_SOURCE, ACCESS_LOG);
        if (Files.exists(accessLog)) {
            Files.delete(accessLog);
            System.out.println("[INFO] 已删除原始 access.log");
        } else {
            System.out.println("[WARN] access.log 不存在，无需删除");
        }

        // 删除 error.log
        Path errorLog = Paths.get(NGINX_LOG_SOURCE, ERROR_LOG);
        if (Files.exists(errorLog)) {
            Files.delete(errorLog);
            System.out.println("[INFO] 已删除原始 error.log");
        } else {
            System.out.println("[WARN] error.log 不存在，无需删除");
        }
    }

    /**
     * 执行 Nginx 重启脚本
     */
    private static void restartNginx() throws IOException, InterruptedException {
        Path restartScript = Paths.get(NGINX_RESTART_CMD);

        if (!Files.exists(restartScript)) {
            System.err.println("[ERROR] 重启脚本不存在: " + NGINX_RESTART_CMD);
            return;
        }

        System.out.println("[INFO] 开始执行 Nginx 重启脚本...");

        // Windows 环境下执行 CMD 文件
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", NGINX_RESTART_CMD);
        processBuilder.directory(new File(NGINX_HOME));

        // 重定向输出流，便于查看脚本执行结果
        processBuilder.inheritIO();

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("[INFO] Nginx 重启脚本执行成功!");
        } else {
            System.out.println("[WARN] Nginx 重启脚本返回退出码: " + exitCode);
        }
    }

    /**
     * 获取当前日期字符串
     */
    public static String getCurrentDateString() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 手动执行单个操作的工具方法
     */
    public static void backupAccessLog() throws IOException {
        String currentDate = getCurrentDateString();
        Path source = Paths.get(NGINX_LOG_SOURCE, ACCESS_LOG);
        Path backup = Paths.get(NGINX_LOG_BACKUP, "access(" + currentDate + ").log");
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[INFO] 已备份 access.log");
    }

    /**
     * 手动执行单个操作的工具方法
     */
    public static void backupErrorLog() throws IOException {
        String currentDate = getCurrentDateString();
        Path source = Paths.get(NGINX_LOG_SOURCE, ERROR_LOG);
        Path backup = Paths.get(NGINX_LOG_BACKUP, "error(" + currentDate + ").log");
        Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[INFO] 已备份 error.log");
    }
}
