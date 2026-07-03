package LDS.Person.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 安全过滤器 - 屏蔽已知的恶意请求和扫描攻击
 * 
 * 功能：
 * 1. 检测和拦截已知的攻击路径
 * 2. 防止日志被恶意请求污染
 * 3. 减少无必要的日志输出
 * 4. 提升应用性能
 * 
 * 注意：IP白名单验证由 IpWhitelistFilter 处理，优先级更高
 */
@Component
@Slf4j
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // 🚫 检测已知的恶意路径 - 直接拒绝，不生成日志
        if (isKnownAttackPath(path)) {
            // 静默拒绝，不记录日志（避免日志污染）
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().close();
            return;
        }
        
        // 🚫 检测可疑的请求参数
        String queryString = httpRequest.getQueryString();
        if (queryString != null && isSuspiciousQuery(queryString)) {
            // 静默拒绝
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().close();
            return;
        }
        
        // ✅ 正常请求，继续处理
        chain.doFilter(request, response);
    }

    /**
     * 检测已知的攻击路径
     * 
     * @param path 请求路径
     * @return 如果是已知攻击路径则返回 true
     */
    private boolean isKnownAttackPath(String path) {
        // 🔍 已知的 PHP 漏洞扫描
        if (path.contains("/phpunit/") || path.contains("/vendor/")) {
            return true;
        }
        
        // 💣 ThinkPHP RCE 攻击
        if (path.contains("/think/app") || path.contains("invokefunction")) {
            return true;
        }
        
        // 🔓 路径穿越攻击
        if (path.contains("../../../../") || path.contains("..\\..\\")) {
            return true;
        }
        
        // 🐳 Docker API 扫描
        if (path.contains("/containers/") || path.contains("/docker/")) {
            return true;
        }
        
        // 📱 扫描常见的 PHP 文件
        if (path.endsWith(".php") || path.endsWith(".phtml") || 
            path.endsWith(".php3") || path.endsWith(".php4") || 
            path.endsWith(".php5") || path.endsWith(".phps")) {
            return true;
        }
        
        // 🔧 扫描常见的配置文件
        if (path.contains("web.config") || path.contains("web.xml") ||
            path.contains(".env") || path.contains("config.php")) {
            return true;
        }
        
        // 🌐 扫描常见的管理后台
        if (path.contains("/admin/") || path.contains("/wp-admin/") ||
            path.contains("/phpmyadmin/") || path.contains("/cpanel/")) {
            return true;
        }
        
        // 📝 扫描常见的备份文件
        if (path.contains(".bak") || path.contains(".backup") ||
            path.contains(".sql") || path.contains(".tar.gz")) {
            return true;
        }
        
        // 🎯 其他已知的扫描路径
        if (path.contains("/eval-stdin.php") || path.contains("/index1") ||
            path.contains("/shell") || path.contains("/webshell")) {
            return true;
        }
        
        return false;
    }

    /**
     * 检测可疑的请求参数
     * 
     * @param queryString 查询字符串
     * @return 如果包含可疑内容则返回 true
     */
    private boolean isSuspiciousQuery(String queryString) {
        // 🔍 检测 SQL 注入
        if (queryString.toLowerCase().contains("union") ||
            queryString.toLowerCase().contains("select") ||
            queryString.toLowerCase().contains("insert") ||
            queryString.toLowerCase().contains("delete") ||
            queryString.toLowerCase().contains("drop")) {
            return true;
        }
        
        // 💣 检测 RCE 攻击
        if (queryString.contains("call_user_func") ||
            queryString.contains("eval") ||
            queryString.contains("exec") ||
            queryString.contains("system")) {
            return true;
        }
        
        // 🔓 检测路径穿越
        if (queryString.contains("../../../../") ||
            queryString.contains("..\\..\\")) {
            return true;
        }
        
        // 📂 检测敏感路径访问
        if (queryString.contains("/tmp") ||
            queryString.contains("/var/www") ||
            queryString.contains("/etc/passwd")) {
            return true;
        }
        
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化过滤器
    }

    @Override
    public void destroy() {
        // 销毁过滤器
    }
}
