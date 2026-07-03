using System.Net;
using Dapper;
using Microsoft.Extensions.Options;
using Npgsql;

namespace SidusX.Config;

/// <summary>
/// 安全过滤中间件 - 屏蔽已知的恶意请求和扫描攻击
/// </summary>
public class SecurityMiddleware
{
    private readonly RequestDelegate _next;

    public SecurityMiddleware(RequestDelegate next)
    {
        _next = next;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var path = context.Request.Path.Value ?? string.Empty;

        if (IsKnownAttackPath(path))
        {
            context.Response.StatusCode = StatusCodes.Status403Forbidden;
            return;
        }

        var query = context.Request.QueryString.Value ?? string.Empty;
        if (!string.IsNullOrEmpty(query) && IsSuspiciousQuery(query))
        {
            context.Response.StatusCode = StatusCodes.Status403Forbidden;
            return;
        }

        await _next(context);
    }

    private static bool IsKnownAttackPath(string path)
    {
        if (path.Contains("/phpunit/") || path.Contains("/vendor/")) return true;
        if (path.Contains("/think/app") || path.Contains("invokefunction")) return true;
        if (path.Contains("../../../../") || path.Contains(@"..\..\..\")) return true;
        if (path.Contains("/containers/") || path.Contains("/docker/")) return true;
        if (path.EndsWith(".php") || path.EndsWith(".phtml") || path.EndsWith(".php3") ||
            path.EndsWith(".php4") || path.EndsWith(".php5") || path.EndsWith(".phps")) return true;
        if (path.Contains("web.config") || path.Contains("web.xml") ||
            path.Contains(".env") || path.Contains("config.php")) return true;
        if (path.Contains("/admin/") || path.Contains("/wp-admin/") ||
            path.Contains("/phpmyadmin/") || path.Contains("/cpanel/")) return true;
        if (path.Contains(".bak") || path.Contains(".backup") ||
            path.Contains(".sql") || path.Contains(".tar.gz")) return true;
        if (path.Contains("/eval-stdin.php") || path.Contains("/index1") ||
            path.Contains("/shell") || path.Contains("/webshell")) return true;
        return false;
    }

    private static bool IsSuspiciousQuery(string query)
    {
        var lower = query.ToLower();
        if (lower.Contains("union") || lower.Contains("select") ||
            lower.Contains("insert") || lower.Contains("delete") || lower.Contains("drop")) return true;
        if (query.Contains("call_user_func") || query.Contains("eval") ||
            query.Contains("exec") || query.Contains("system")) return true;
        if (query.Contains("../../../../") || query.Contains(@"..\..\..\")) return true;
        if (query.Contains("/tmp") || query.Contains("/var/www") || query.Contains("/etc/passwd")) return true;
        return false;
    }
}

/// <summary>
/// IP白名单中间件 - 限制接口只能通过白名单IP访问，或通过有效的pass_token绕过
/// </summary>
public class IpWhitelistMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<IpWhitelistMiddleware> _logger;
    private readonly SecuritySettings _settings;
    private readonly IServiceProvider _serviceProvider;

    public IpWhitelistMiddleware(
        RequestDelegate next,
        ILogger<IpWhitelistMiddleware> logger,
        IOptions<SecuritySettings> settings,
        IServiceProvider serviceProvider)
    {
        _next = next;
        _logger = logger;
        _settings = settings.Value;
        _serviceProvider = serviceProvider;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        // 直接放行CORS预检请求
        if (HttpMethods.IsOptions(context.Request.Method))
        {
            context.Response.StatusCode = StatusCodes.Status200OK;
            await _next(context);
            return;
        }

        // 如果IP白名单功能已禁用，直接放行
        if (!_settings.IpWhitelistEnabled)
        {
            await _next(context);
            return;
        }

        var clientIp = GetClientRealIp(context);
        var path = context.Request.Path.Value ?? string.Empty;

        bool isIpAllowed = IsIpWhitelisted(clientIp);
        bool isTokenValid = IsPassTokenValid(context);

        // 检查是否有BypassIpWhitelist标注（公开接口）
        var endpoint = context.GetEndpoint();
        if (endpoint != null)
        {
            var bypassAttr = endpoint.Metadata.GetMetadata<BypassIpWhitelistAttribute>();
            if (bypassAttr != null)
            {
                _logger.LogDebug("公开接口访问 - IP: {Ip}, 路径: {Path}, 原因: {Reason}", clientIp, path, bypassAttr.Reason);
                await LogAccess(clientIp, path, 1);
                await _next(context);
                return;
            }
        }

        if (!isIpAllowed && !isTokenValid)
        {
            _logger.LogWarning("拒绝API请求 - IP: {Ip}, 路径: {Path}, 原因: IP不在白名单且pass_token无效", clientIp, path);
            await LogAccess(clientIp, path, 0);
            context.Response.StatusCode = StatusCodes.Status403Forbidden;
            context.Response.ContentType = "application/json;charset=UTF-8";
            await context.Response.WriteAsync("{\"error\":\"Access denied: IP whitelist or pass_token is required\"}");
            return;
        }

        _logger.LogDebug("IP白名单或pass_token验证通过 - IP: {Ip}, 路径: {Path}", clientIp, path);
        await LogAccess(clientIp, path, 1);
        await _next(context);
    }

    private string GetClientRealIp(HttpContext context)
    {
        var headers = context.Request.Headers;
        if (headers.TryGetValue("X-Real-IP", out var realIp) && !string.IsNullOrEmpty(realIp))
            return realIp.ToString().Trim();
        if (headers.TryGetValue("X-Forwarded-For", out var forwarded) && !string.IsNullOrEmpty(forwarded))
            return forwarded.ToString().Split(',')[0].Trim();
        return context.Connection.RemoteIpAddress?.ToString() ?? "unknown";
    }

    private bool IsIpWhitelisted(string clientIp)
    {
        var whitelist = _settings.IpWhitelist;
        if (whitelist == null || whitelist.Count == 0) return false;
        return whitelist.Any(ip => ip.Equals(clientIp, StringComparison.OrdinalIgnoreCase));
    }

    private bool IsPassTokenValid(HttpContext context)
    {
        if (!_settings.PassTokenEnabled) return false;
        var tokens = _settings.GetPassTokenSet();
        if (tokens.Count == 0) return false;

        // 从Header检查
        if (context.Request.Headers.TryGetValue("X-Pass-Token", out var headerToken) &&
            tokens.Contains(headerToken.ToString())) return true;

        // 从Query检查
        if (context.Request.Query.TryGetValue("pass_token", out var queryToken) &&
            tokens.Contains(queryToken.ToString())) return true;

        return false;
    }

    private async Task LogAccess(string ip, string api, int states)
    {
        if (api == "/favicon.ico") return;
        if (api == "/error") return;

        try
        {
            using var scope = _serviceProvider.CreateScope();
            var db = scope.ServiceProvider.GetRequiredService<Npgsql.NpgsqlConnection>();
            await db.OpenAsync();
            await db.ExecuteAsync(
                "INSERT INTO api_log (ip, api, states, create_time) VALUES (@ip, @api, @states, @createTime)",
                new { ip, api, states, createTime = DateTime.Now });
        }
        catch (Exception ex)
        {
            _logger.LogWarning("记录访问日志失败: {Message}", ex.Message);
        }
    }
}
