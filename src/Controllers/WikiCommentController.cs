using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/wiki/comment")]
public class WikiCommentController : ControllerBase
{
    private readonly WikiCommentService _service;
    public WikiCommentController(WikiCommentService service) => _service = service;

    [HttpPost("create")]
    public async Task<R<WikiCommentResponse>> Create([FromBody] WikiCommentCreateRequest req)
    {
        try { return R<WikiCommentResponse>.Ok(await _service.CreateAsync(req)); }
        catch (ArgumentException e) { return R<WikiCommentResponse>.Error(e.Message); }
    }

    [HttpDelete("delete/{replyId}")]
    public async Task<R<bool>> Delete(long replyId) => R<bool>.Ok(await _service.DeleteAsync(replyId));

    [HttpPost("like/{replyId}")]
    public async Task<R<string>> Like(long replyId) { await _service.LikeAsync(replyId); return R<string>.Ok("点赞成功"); }

    [HttpPost("page")]
    public async Task<R<PageResponse<WikiCommentResponse>>> Page([FromBody] WikiCommentByWikiIdRequest req)
        => R<PageResponse<WikiCommentResponse>>.Ok(await _service.PageByWikiIdAsync(req));
}
