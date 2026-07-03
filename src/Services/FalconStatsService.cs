using System.Text.Json;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class FalconStatsService
{
    private readonly FalconStatsRepository _repo;
    private readonly IHttpClientFactory _http;
    private readonly ILogger<FalconStatsService> _logger;

    public FalconStatsService(FalconStatsRepository repo, IHttpClientFactory http, ILogger<FalconStatsService> logger)
    { _repo = repo; _http = http; _logger = logger; }

    public async Task<FalconStats?> GetLatestAsync() => await _repo.GetLatestAsync();

    public async Task FetchAndSaveAsync()
    {
        try
        {
            var client = _http.CreateClient("spacex");
            var response = await client.PostAsync("https://api.spacexdata.com/v4/cores/query",
                new StringContent("{\"query\":{},\"options\":{\"pagination\":false}}", System.Text.Encoding.UTF8, "application/json"));
            response.EnsureSuccessStatusCode();
            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);
            var docs = doc.RootElement.GetProperty("docs");
            int launches = 0, landings = 0, reflights = 0;
            string docId = string.Empty;
            foreach (var d in docs.EnumerateArray())
            {
                launches += d.TryGetProperty("reuse_count", out var rc) ? rc.GetInt32() + 1 : 1;
                if (d.TryGetProperty("landings", out var lArr)) landings += lArr.EnumerateArray().Count();
                if (d.TryGetProperty("reuse_count", out var rf) && rf.GetInt32() > 0) reflights += rf.GetInt32();
                if (string.IsNullOrEmpty(docId) && d.TryGetProperty("id", out var id)) docId = id.GetString() ?? string.Empty;
            }
            await _repo.InsertAsync(new FalconStats { DocumentId = docId, TotalLaunches = launches, TotalLandings = landings, TotalReflights = reflights, CreatedAt = DateTime.Now });
            _logger.LogInformation("Falcon统计数据已更新 - 发射:{Launches}, 着陆:{Landings}, 复用:{Reflights}", launches, landings, reflights);
        }
        catch (Exception ex) { _logger.LogError(ex, "获取Falcon统计数据失败"); }
    }
}
