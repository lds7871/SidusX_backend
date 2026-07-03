using SidusX.Services;

namespace SidusX.BackgroundTasks;

/// <summary>每6小时获取一次最近发射数据</summary>
public class RecentLaunchTask : BackgroundService
{
    private readonly ILogger<RecentLaunchTask> _logger;
    private readonly IServiceScopeFactory _scopeFactory;
    private static readonly TimeSpan Interval = TimeSpan.FromHours(6);

    public RecentLaunchTask(ILogger<RecentLaunchTask> logger, IServiceScopeFactory scopeFactory)
    {
        _logger = logger; _scopeFactory = scopeFactory;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        _logger.LogInformation("最近发射定时任务已启动");
        // 首次立即执行
        await RunAsync();
        using var timer = new PeriodicTimer(Interval);
        while (await timer.WaitForNextTickAsync(stoppingToken))
        {
            await RunAsync();
        }
    }

    private async Task RunAsync()
    {
        try
        {
            using var scope = _scopeFactory.CreateScope();
            var service = scope.ServiceProvider.GetRequiredService<RecentLaunchService>();
            await service.FetchAndSaveAsync();
        }
        catch (Exception ex) { _logger.LogError(ex, "最近发射数据任务执行失败"); }
    }
}
