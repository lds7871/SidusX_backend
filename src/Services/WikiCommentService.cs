using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;

namespace SidusX.Services;

public class WikiCommentService
{
    private readonly WikiCommentRepository _repo;
    private readonly UserRepository _userRepo;
    public WikiCommentService(WikiCommentRepository repo, UserRepository userRepo) { _repo = repo; _userRepo = userRepo; }

    public async Task<WikiCommentResponse> CreateAsync(WikiCommentCreateRequest req)
    {
        if (string.IsNullOrWhiteSpace(req.Text)) throw new ArgumentException("留言内容不能为空");
        var c = new WikiComment { WikiId = req.WikiId, UserId = req.UserId, Text = req.Text, CreateTime = DateTime.Now };
        c.ReplyId = await _repo.InsertAsync(c);
        return new WikiCommentResponse { ReplyId = c.ReplyId, WikiId = c.WikiId, UserId = c.UserId, Text = c.Text, CreateTime = c.CreateTime };
    }

    public async Task<bool> DeleteAsync(long replyId) => await _repo.DeleteAsync(replyId);

    public async Task LikeAsync(long replyId) => await _repo.IncrementLikesAsync(replyId);

    public async Task<PageResponse<WikiCommentResponse>> PageByWikiIdAsync(WikiCommentByWikiIdRequest req)
    {
        var (items, total) = await _repo.PageByWikiIdAsync(req.WikiId, req.Page, req.PageSize);
        var responses = new List<WikiCommentResponse>();
        foreach (var item in items)
        {
            var name = await _userRepo.GetNameByIdAsync(item.UserId);
            responses.Add(new WikiCommentResponse { ReplyId = item.ReplyId, WikiId = item.WikiId, UserId = item.UserId, UserName = name, Text = item.Text, Likes = item.Likes, CreateTime = item.CreateTime });
        }
        return new PageResponse<WikiCommentResponse> { Total = total, Page = req.Page, PageSize = req.PageSize, Data = responses };
    }
}
