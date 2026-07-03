using SidusX.Services;

namespace SidusX.BackgroundTasks;

/// <summary>每天定时获取NASA每日图片</summary>
public class NasaDailyImageTask : BackgroundService
{
    private readonly ILogger<NasaDailyImageTask> _logger;
    private readonly IServiceScopeFactory _scopeFactory;

    public NasaDailyImageTask(ILogger<NasaDailyImageTask> logger, IServiceScopeFactory scopeFactory)
    {
        _logger = logger; _scopeFactory = scopeFactory;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("NASA每日图片定时任务已启动");
        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                using var scope = _scopeFactory.CreateScope();
                var service = scope.ServiceProvider.GetRequiredService<NasaDailyImageService>();
                await service.FetchAndSaveAsync();
            }
            catch (Exception ex) { _logger.LogError(ex, "NASA每日图片任务执行失败"); }

            // 计算距离明天 08:00 的等待时间
            var now = DateTime.Now;
            var next = now.Date.AddDays(1).AddHours(8);
            var delay = next - now;
            _logger.LogInformation("NASA图片任务完成，下次执行时间: {Next}", next);
            await Task.Delay(delay, stoppingToken);
        }
    }
}
