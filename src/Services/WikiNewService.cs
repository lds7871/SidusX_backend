using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class WikiNewService
{
    private readonly WikiNewRepository _repo;
    private readonly WikiRepository _wikiRepo;
    public WikiNewService(WikiNewRepository repo, WikiRepository wikiRepo) { _repo = repo; _wikiRepo = wikiRepo; }

    public async Task<WikiNewResponse> CreateAsync(WikiNewCreateRequest req)
    {
        if (string.IsNullOrWhiteSpace(req.KeyName)) throw new ArgumentException("key_name 不能为空");
        if (string.IsNullOrWhiteSpace(req.Texts)) throw new ArgumentException("texts 不能为空");
        if (await _wikiRepo.IsKeyNameExistsAsync(req.KeyName)) throw new ArgumentException($"wiki中已有此key_name: {req.KeyName}");
        if (await _repo.IsKeyNameExistsAsync(req.KeyName)) throw new ArgumentException($"wiki_new中已有此key_name: {req.KeyName}");
        var now = DateTime.Now;
        var n = new WikiNew { KeyName = req.KeyName, Texts = req.Texts, Tags = req.Tags, Version = 1.0, CreateTime = now, CreateUser = req.CreateUser, UpdateTime = now, UpdateUser = req.CreateUser, WikiStates = 0 };
        n.WikinewId = await _repo.InsertAsync(n);
        return ToResponse(n);
    }

    public async Task<WikiNewResponse?> GetByIdAsync(long id)
    {
        var n = await _repo.GetByIdAsync(id);
        return n == null ? null : ToResponse(n);
    }

    public async Task<PageResponse<WikiNewListResponse>> PageQueryAsync(WikiNewPageQueryRequest req)
    {
        var (items, total) = await _repo.PageQueryAsync(req);
        return new PageResponse<WikiNewListResponse>
        {
            Total = total, Page = req.Page, PageSize = req.PageSize,
            Data = items.Select(n => new WikiNewListResponse { WikinewId = n.WikinewId, KeyName = n.KeyName, WikiStates = n.WikiStates, CreateTime = n.CreateTime, CreateUser = n.CreateUser }).ToList()
        };
    }

    public async Task<WikiNewReviewResponse?> ReviewAsync(WikiNewReviewRequest req)
    {
        var n = await _repo.GetByIdAsync(req.WikinewId);
        if (n == null) return null;
        await _repo.UpdateStateAsync(req.WikinewId, req.WikiStates);
        // If approved, insert into wiki
        if (req.WikiStates == 1)
        {
            var now = DateTime.Now;
            var wiki = new Wiki { KeyName = n.KeyName, Texts = n.Texts, Tags = n.Tags, Version = 1.0, CreateTime = now, CreateUser = n.CreateUser, UpdateTime = now, UpdateUser = n.CreateUser };
            await _wikiRepo.InsertAsync(wiki);
        }
        return new WikiNewReviewResponse { WikinewId = req.WikinewId, WikiStates = req.WikiStates, Message = req.WikiStates == 1 ? "审核通过，已创建Wiki" : "已拒绝" };
    }

    private static WikiNewResponse ToResponse(WikiNew n) => new()
    {
        WikinewId = n.WikinewId, KeyName = n.KeyName, Texts = n.Texts, Tags = n.Tags,
        Version = n.Version, CreateTime = n.CreateTime, CreateUser = n.CreateUser,
        UpdateTime = n.UpdateTime, UpdateUser = n.UpdateUser, WikiStates = n.WikiStates
    };
}
