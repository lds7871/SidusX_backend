using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using Dapper;
using Npgsql;

namespace SidusX.Config;

/// <summary>
/// API日志中间件 - 记录以/GHapi开头的请求和响应
/// </summary>
public class ApiLogMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ApiLogMiddleware> _logger;
    private readonly IServiceProvider _serviceProvider;
    private const int MaxBodyLength = 2000;
    private const int MaxResponseLength = 200;

    private static readonly JsonSerializerOptions JsonOptions = new()
    {
        WriteIndented = true,
        DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull
    };

    public ApiLogMiddleware(
        RequestDelegate next,
        ILogger<ApiLogMiddleware> logger,
        IServiceProvider serviceProvider)
    {
        _next = next;
        _logger = logger;
        _serviceProvider = serviceProvider;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        var path = context.Request.Path.Value ?? string.Empty;
        if (!path.StartsWith("/GHapi"))
        {
            await _next(context);
            return;
        }

        // 缓冲请求体
        context.Request.EnableBuffering();

        var originalBody = context.Response.Body;
        using var responseBuffer = new MemoryStream();
        context.Response.Body = responseBuffer;

        var sw = System.Diagnostics.Stopwatch.StartNew();
        Exception? exception = null;
        try
        {
            await _next(context);
        }
        catch (Exception ex)
        {
            exception = ex;
            throw;
        }
        finally
        {
            sw.Stop();
            await RecordLog(context, sw.ElapsedMilliseconds, exception);

            // 将响应体复制回原始流
            responseBuffer.Seek(0, SeekOrigin.Begin);
            await responseBuffer.CopyToAsync(originalBody);
            context.Response.Body = originalBody;
        }
    }

    private async Task RecordLog(HttpContext context, long durationMs, Exception? exception)
    {
        try
        {
            var logData = new Dictionary<string, object?>
            {
                ["logged_at"] = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.fff"),
                ["method"] = context.Request.Method,
                ["path"] = context.Request.Path.Value,
                ["duration_ms"] = durationMs,
                ["status"] = context.Response.StatusCode < 400 ? "success" : "error",
                ["status_code"] = context.Response.StatusCode
            };

            // 读取请求体
            try
            {
                context.Request.Body.Seek(0, SeekOrigin.Begin);
                using var reader = new StreamReader(context.Request.Body, Encoding.UTF8, leaveOpen: true);
                var body = await reader.ReadToEndAsync();
                context.Request.Body.Seek(0, SeekOrigin.Begin);
                if (!string.IsNullOrWhiteSpace(body))
                {
                    var contentType = context.Request.ContentType ?? string.Empty;
                    if (contentType.Contains("application/json"))
                    {
                        try
                        {
                            var parsed = JsonSerializer.Deserialize<JsonElement>(body);
                            logData["request_body"] = parsed;
                        }
                        catch
                        {
                            logData["request_body"] = Truncate(body, MaxBodyLength);
                        }
                    }
                    else
                    {
                        logData["request_body"] = Truncate(body, MaxBodyLength);
                    }
                }
            }
            catch { /* ignore */ }

            // 读取响应体
            try
            {
                if (context.Response.Body is MemoryStream ms)
                {
                    ms.Seek(0, SeekOrigin.Begin);
                    using var reader = new StreamReader(ms, Encoding.UTF8, leaveOpen: true);
                    var body = await reader.ReadToEndAsync();
                    ms.Seek(0, SeekOrigin.Begin);
                    if (!string.IsNullOrWhiteSpace(body))
                    {
                        logData["response"] = Truncate(body, MaxResponseLength);
                    }
                }
            }
            catch { /* ignore */ }

            if (exception != null)
            {
                logData["error"] = new { exception_type = exception.GetType().Name, message = exception.Message };
            }

            var json = JsonSerializer.Serialize(logData, JsonOptions);
            _logger.LogInformation("\n========== API LOG ==========\n{Log}\n========== END LOG ==========", json);

            // 写入数据库
            try
            {
                using var scope = _serviceProvider.CreateScope();
                var db = scope.ServiceProvider.GetRequiredService<NpgsqlConnection>();
                await db.OpenAsync();
                await db.ExecuteAsync(
                    "INSERT INTO api_raw_logs(raw_json) VALUES (@json::jsonb)",
                    new { json });
            }
            catch (Exception dbEx)
            {
                _logger.LogWarning("Failed to save API log to database: {Message}", dbEx.Message);
            }
        }
        catch (Exception ex)
        {
            _logger.LogWarning("Failed to serialize log data: {Message}", ex.Message);
        }
    }

    private static string Truncate(string s, int maxLen) =>
        s.Length <= maxLen ? s : s[..maxLen] + "...(truncated)";
}
