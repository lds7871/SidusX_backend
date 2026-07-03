using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class ArticleService
{
    private readonly ArticleRepository _repo;
    private readonly ILogger<ArticleService> _logger;

    public ArticleService(ArticleRepository repo, ILogger<ArticleService> logger)
    {
        _repo = repo; _logger = logger;
    }

    public async Task<ArticleResponse> CreateAsync(ArticleCreateRequest req)
    {
        if (string.IsNullOrWhiteSpace(req.Title)) throw new ArgumentException("标题不能为空");
        if (string.IsNullOrWhiteSpace(req.Texts)) throw new ArgumentException("内容不能为空");
        var now = DateTime.Now;
        var article = new Article { Title = req.Title, Cover = req.Cover, Info = req.Info, Texts = req.Texts, Tags = req.Tags, CreateTime = now, UpdateTime = now };
        article.ArticleId = await _repo.InsertAsync(article);
        return ToResponse(article);
    }

    public async Task<bool> DeleteAsync(long id) => await _repo.DeleteAsync(id);

    public async Task<ArticleResponse?> GetByIdAsync(long id)
    {
        var a = await _repo.GetByIdAsync(id);
        return a == null ? null : ToResponse(a);
    }

    public async Task<ArticleResponse?> UpdateAsync(long id, ArticleCreateRequest req)
    {
        var existing = await _repo.GetByIdAsync(id);
        if (existing == null) return null;
        existing.Title = req.Title; existing.Cover = req.Cover; existing.Info = req.Info;
        existing.Texts = req.Texts; existing.Tags = req.Tags;
        await _repo.UpdateAsync(existing);
        return ToResponse(existing);
    }

    public async Task<ArticlePageResponse> PageQueryAsync(ArticleQueryRequest req)
    {
        var (items, total) = await _repo.PageQueryAsync(req);
        return new ArticlePageResponse
        {
            Total = total, Page = req.Page, PageSize = req.PageSize,
            Data = items.Select(a => new ArticleListResponse { ArticleId = a.ArticleId, Title = a.Title, Cover = a.Cover, Info = a.Info, Tags = a.Tags, CreateTime = a.CreateTime, UpdateTime = a.UpdateTime }).ToList()
        };
    }

    public async Task<List<ArticleLatestResponse>> GetLatestAsync(int limit)
    {
        var items = await _repo.GetLatestAsync(limit);
        return items.Select(a => new ArticleLatestResponse { ArticleId = a.ArticleId, Title = a.Title, Cover = a.Cover, CreateTime = a.CreateTime }).ToList();
    }

    private static ArticleResponse ToResponse(Article a) => new()
    {
        ArticleId = a.ArticleId, Title = a.Title, Cover = a.Cover, Info = a.Info,
        Texts = a.Texts, Tags = a.Tags, CreateTime = a.CreateTime, UpdateTime = a.UpdateTime
    };
}
