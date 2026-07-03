using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class WikiReviewService
{
    private readonly WikiReviewRepository _repo;
    private readonly WikiRepository _wikiRepo;
    private readonly WikiHistoryRepository _historyRepo;
    public WikiReviewService(WikiReviewRepository repo, WikiRepository wikiRepo, WikiHistoryRepository historyRepo)
    { _repo = repo; _wikiRepo = wikiRepo; _historyRepo = historyRepo; }

    public async Task<WikiReviewResponse> CreateAsync(WikiReviewCreateRequest req)
    {
        var wiki = await _wikiRepo.GetByIdAsync(req.WikiId) ?? throw new ArgumentException("Wiki不存在");
        var r = new WikiReview { WikiId = req.WikiId, Texts = req.Texts, Tags = req.Tags, Version = wiki.Version + 0.01, UpdateTime = DateTime.Now, UpdateUser = req.UpdateUser, WikiStates = 0 };
        r.WikireviewId = await _repo.InsertAsync(r);
        return ToResponse(r);
    }

    public async Task<PageResponse<WikiReviewListResponse>> PageQueryAsync(WikiReviewPageQueryRequest req)
    {
        var (items, total) = await _repo.PageQueryAsync(req);
        return new PageResponse<WikiReviewListResponse>
        {
            Total = total, Page = req.Page, PageSize = req.PageSize,
            Data = items.Select(r => new WikiReviewListResponse { WikireviewId = r.WikireviewId, WikiId = r.WikiId, WikiStates = r.WikiStates, UpdateTime = r.UpdateTime, UpdateUser = r.UpdateUser }).ToList()
        };
    }

    public async Task<WikiReviewResponse?> UpdateStateAsync(WikiReviewUpdateRequest req)
    {
        var review = await _repo.GetByIdAsync(req.WikireviewId);
        if (review == null) return null;
        await _repo.UpdateStateAsync(req.WikireviewId, req.WikiStates);
        // If approved, update wiki
        if (req.WikiStates == 1)
        {
            var wiki = await _wikiRepo.GetByIdAsync(review.WikiId);
            if (wiki != null)
            {
                await _historyRepo.InsertAsync(new WikiHistory { WikiId = wiki.WikiId, KeyName = wiki.KeyName, Texts = wiki.Texts, Tags = wiki.Tags, Version = wiki.Version, CreateTime = wiki.CreateTime, CreateUser = wiki.CreateUser, UpdateTime = wiki.UpdateTime, UpdateUser = wiki.UpdateUser, BackupTime = DateTime.Now });
                await _wikiRepo.UpdateAsync(wiki.WikiId, review.Texts, review.Tags, review.UpdateUser);
            }
        }
        review.WikiStates = req.WikiStates;
        return ToResponse(review);
    }

    private static WikiReviewResponse ToResponse(WikiReview r) => new()
    {
        WikireviewId = r.WikireviewId, WikiId = r.WikiId, Texts = r.Texts, Tags = r.Tags,
        Version = r.Version, UpdateTime = r.UpdateTime, UpdateUser = r.UpdateUser, WikiStates = r.WikiStates
    };
}
