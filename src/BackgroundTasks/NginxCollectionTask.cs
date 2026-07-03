using SidusX.Utils;

namespace SidusX.BackgroundTasks;

/// <summary>每天定时收集Nginx日志摘要</summary>
public class NginxCollectionTask : BackgroundService
{
    private readonly ILogger<NginxCollectionTask> _logger;

    public NginxCollectionTask(ILogger<NginxCollectionTask> logger) => _logger = logger;

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("Nginx日志收集任务已启动");
        while (!stoppingToken.IsCancellationRequested)
        {
            var now = DateTime.Now;
            // 每天 03:00 执行
            var next = now.Date.AddDays(1).AddHours(3);
            if (now.Hour < 3) next = now.Date.AddHours(3);
            var delay = next - now;
            await Task.Delay(delay, stoppingToken);
            if (stoppingToken.IsCancellationRequested) break;
            try
            {
                var (exists, path, lines, total) = NginxLogManager.ReadLog(int.MaxValue);
                _logger.LogInformation("Nginx日志收集完成 - 路径: {Path}, 总行数: {Total}, 存在: {Exists}", path, total, exists);
            }
            catch (Exception ex) { _logger.LogError(ex, "Nginx日志收集失败"); }
        }
    }
}
