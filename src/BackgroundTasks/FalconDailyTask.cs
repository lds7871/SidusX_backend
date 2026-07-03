using SidusX.Services;

namespace SidusX.BackgroundTasks;

/// <summary>每天 02:05 获取 Falcon 统计数据</summary>
public class FalconDailyTask : BackgroundService
{
    private readonly ILogger<FalconDailyTask> _logger;
    private readonly IServiceScopeFactory _scopeFactory;

    public FalconDailyTask(ILogger<FalconDailyTask> logger, IServiceScopeFactory scopeFactory)
    {
        _logger = logger; _scopeFactory = scopeFactory;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("Falcon统计定时任务已启动");
        while (!stoppingToken.IsCancellationRequested)
        {
            var now = DateTime.Now;
            var next = now.Date.AddDays(1).AddHours(2).AddMinutes(5);
            if (now.Hour < 2 || (now.Hour == 2 && now.Minute < 5))
                next = now.Date.AddHours(2).AddMinutes(5);
            var delay = next - now;
            await Task.Delay(delay, stoppingToken);
            if (stoppingToken.IsCancellationRequested) break;
            try
            {
                using var scope = _scopeFactory.CreateScope();
                var service = scope.ServiceProvider.GetRequiredService<FalconStatsService>();
                await service.FetchAndSaveAsync();
                _logger.LogInformation("Falcon统计任务执行完成");
            }
            catch (Exception ex) { _logger.LogError(ex, "Falcon统计任务执行失败"); }
        }
    }
}
