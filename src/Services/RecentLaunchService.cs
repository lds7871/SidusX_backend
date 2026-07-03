using SidusX.DTOs.Response;
using SidusX.Repositories;

namespace SidusX.Services;

public class RecentLaunchService
{
    private readonly RecentLaunchRepository _repo;
    private readonly IHttpClientFactory _http;
    private readonly ILogger<RecentLaunchService> _logger;
    public RecentLaunchService(RecentLaunchRepository repo, IHttpClientFactory http, ILogger<RecentLaunchService> logger)
    { _repo = repo; _http = http; _logger = logger; }

    public async Task<RecentLaunchDataResponse?> GetLatestAsync()
    {
        var r = await _repo.GetLatestAsync();
        return r == null ? null : new RecentLaunchDataResponse { Id = r.Id, Data = r.Data, GetTime = r.GetTime };
    }

    public async Task FetchAndSaveAsync()
    {
        try
        {
            var client = _http.CreateClient("spacex");
            var json = await client.GetStringAsync("https://api.spacexdata.com/v5/launches/latest");
            await _repo.InsertAsync(json);
            _logger.LogInformation("最近发射数据已更新");
        }
        catch (Exception ex) { _logger.LogError(ex, "获取最近发射数据失败"); }
    }
}
