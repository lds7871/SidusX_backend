using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class WikiService
{
    private readonly WikiRepository _wikiRepo;
    private readonly WikiHistoryRepository _historyRepo;
    private readonly UserRepository _userRepo;
    private readonly ILogger<WikiService> _logger;

    public WikiService(WikiRepository wikiRepo, WikiHistoryRepository historyRepo,
        UserRepository userRepo, ILogger<WikiService> logger)
    {
        _wikiRepo = wikiRepo;
        _historyRepo = historyRepo;
        _userRepo = userRepo;
        _logger = logger;
    }

    public async Task<WikiResponse> CreateWikiAsync(WikiCreateRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.KeyName)) throw new ArgumentException("key_name 不能为空");
        if (string.IsNullOrWhiteSpace(request.Texts)) throw new ArgumentException("texts 不能为空");
        if (await _wikiRepo.IsKeyNameExistsAsync(request.KeyName)) throw new ArgumentException($"key_name '{request.KeyName}' 已存在");

        var now = DateTime.Now;
        var wiki = new Wiki
        {
            KeyName = request.KeyName, Texts = request.Texts, Tags = request.Tags,
            Version = 1.0, CreateTime = now, CreateUser = request.CreateUser ?? "system",
            UpdateTime = now, UpdateUser = request.UpdateUser ?? request.CreateUser ?? "system"
        };
        wiki.WikiId = await _wikiRepo.InsertAsync(wiki);
        return ToResponse(wiki);
    }

    public async Task<bool> DeleteWikiAsync(long wikiId)
    {
        if (wikiId <= 0) throw new ArgumentException("wikiId 不合法");
        return await _wikiRepo.DeleteAsync(wikiId);
    }

    public async Task<WikiResponse?> GetByIdAsync(long wikiId)
    {
        var wiki = await _wikiRepo.GetByIdAsync(wikiId);
        return wiki == null ? null : ToResponse(wiki);
    }

    public async Task<WikiResponse?> GetByKeyNameAsync(string keyName)
    {
        var wiki = await _wikiRepo.GetByKeyNameAsync(keyName);
        return wiki == null ? null : ToResponse(wiki);
    }

    public async Task<bool> IsKeyNameExistsAsync(string keyName) =>
        await _wikiRepo.IsKeyNameExistsAsync(keyName);

    public async Task<WikiResponse?> UpdateWikiAsync(WikiUpdateRequest request)
    {
        if (request.WikiId <= 0) throw new ArgumentException("wikiId 不合法");
        var existing = await _wikiRepo.GetByIdAsync(request.WikiId);
        if (existing == null) return null;

        // Save to history first
        await _historyRepo.InsertAsync(new WikiHistory
        {
            WikiId = existing.WikiId, KeyName = existing.KeyName, Texts = existing.Texts,
            Tags = existing.Tags, Version = existing.Version, CreateTime = existing.CreateTime,
            CreateUser = existing.CreateUser, UpdateTime = existing.UpdateTime,
            UpdateUser = existing.UpdateUser, BackupTime = DateTime.Now
        });

        await _wikiRepo.UpdateAsync(existing.WikiId,
            request.Texts ?? existing.Texts,
            request.Tags ?? existing.Tags,
            request.UpdateUser);

        var updated = await _wikiRepo.GetByIdAsync(existing.WikiId);
        return updated == null ? null : ToResponse(updated);
    }

    public async Task<PageResponse<WikiResponse>> PageQueryAsync(WikiPageQueryRequest request)
    {
        var (items, total) = await _wikiRepo.PageQueryAsync(request);
        return new PageResponse<WikiResponse>
        {
            Total = total, Page = request.Page, PageSize = request.PageSize,
            Data = items.Select(ToResponse).ToList()
        };
    }

    public async Task<List<LatestWikiSummaryResponse>> GetLatestAsync(int limit = 10)
    {
        var items = await _wikiRepo.GetLatestAsync(limit);
        return items.Select(w => new LatestWikiSummaryResponse
        {
            WikiId = w.WikiId, KeyName = w.KeyName, UpdateTime = w.UpdateTime, UpdateUser = w.UpdateUser
        }).ToList();
    }

    private static WikiResponse ToResponse(Wiki w) => new()
    {
        WikiId = w.WikiId, KeyName = w.KeyName, Texts = w.Texts, Tags = w.Tags,
        Version = w.Version, CreateTime = w.CreateTime, CreateUser = w.CreateUser,
        UpdateTime = w.UpdateTime, UpdateUser = w.UpdateUser
    };
}
