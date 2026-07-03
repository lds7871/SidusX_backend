using System.Text.Json;
using Microsoft.Extensions.Options;
using SidusX.Config;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class NasaDailyImageService
{
    private readonly NasaDailyImageRepository _repo;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly ILogger<NasaDailyImageService> _logger;
    private readonly ProxySettings _proxy;

    public NasaDailyImageService(NasaDailyImageRepository repo, IHttpClientFactory httpClientFactory,
        ILogger<NasaDailyImageService> logger, IOptions<ProxySettings> proxy)
    {
        _repo = repo; _httpClientFactory = httpClientFactory; _logger = logger; _proxy = proxy.Value;
    }

    public async Task<NasaDailyImageDetailResponse?> GetLatestAsync()
    {
        var img = await _repo.GetLatestAsync();
        return img == null ? null : ToDetailResponse(img);
    }

    public async Task<NasaDailyImageDetailResponse?> GetByIdAsync(long id)
    {
        var img = await _repo.GetByIdAsync(id);
        return img == null ? null : ToDetailResponse(img);
    }

    public async Task<PageResponse<NasaDailyImageListResponse>> PageQueryAsync(int page, int pageSize)
    {
        var (items, total) = await _repo.PageQueryAsync(page, pageSize);
        return new PageResponse<NasaDailyImageListResponse>
        {
            Total = total, Page = page, PageSize = pageSize,
            Data = items.Select(i => new NasaDailyImageListResponse { ApodId = i.ApodId, Title = i.Title, Url = i.Url, MediaType = i.MediaType, CreateTime = i.CreateTime }).ToList()
        };
    }

    public async Task FetchAndSaveAsync()
    {
        if (await _repo.IsTodayImageExistsAsync()) { _logger.LogInformation("今日NASA图片已存在，跳过获取"); return; }
        try
        {
            var apiKey = Environment.GetEnvironmentVariable("NASA_API_KEY") ?? "DEMO_KEY";
            var client = _httpClientFactory.CreateClient("nasa");
            var response = await client.GetAsync($"https://api.nasa.gov/planetary/apod?api_key={apiKey}");
            response.EnsureSuccessStatusCode();
            var json = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(json);
            var root = doc.RootElement;
            var img = new NasaDailyImage
            {
                Copyright = root.TryGetProperty("copyright", out var cp) ? cp.GetString() : null,
                Explanation = root.GetProperty("explanation").GetString() ?? string.Empty,
                MediaType = root.GetProperty("media_type").GetString() ?? "image",
                Title = root.GetProperty("title").GetString() ?? string.Empty,
                Url = root.GetProperty("url").GetString() ?? string.Empty,
                CreateTime = DateTime.Now
            };
            await _repo.InsertAsync(img);
            _logger.LogInformation("NASA每日图片获取成功: {Title}", img.Title);
        }
        catch (Exception ex) { _logger.LogError(ex, "获取NASA每日图片失败"); }
    }

    private static NasaDailyImageDetailResponse ToDetailResponse(NasaDailyImage i) => new()
    {
        ApodId = i.ApodId, Copyright = i.Copyright, Explanation = i.Explanation,
        MediaType = i.MediaType, Title = i.Title, Url = i.Url, CreateTime = i.CreateTime
    };
}
