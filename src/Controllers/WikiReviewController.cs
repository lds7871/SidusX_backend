using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/wikireview")]
public class WikiReviewController : ControllerBase
{
    private readonly WikiReviewService _service;
    public WikiReviewController(WikiReviewService service) => _service = service;

    [HttpPost("create")]
    public async Task<R<WikiReviewResponse>> Create([FromBody] WikiReviewCreateRequest req)
    {
        try { return R<WikiReviewResponse>.Ok(await _service.CreateAsync(req)); }
        catch (ArgumentException e) { return R<WikiReviewResponse>.Error(e.Message); }
    }

    [HttpPost("page")]
    public async Task<R<PageResponse<WikiReviewListResponse>>> Page([FromBody] WikiReviewPageQueryRequest req)
        => R<PageResponse<WikiReviewListResponse>>.Ok(await _service.PageQueryAsync(req));

    [HttpPost("state/update")]
    public async Task<R<WikiReviewResponse?>> UpdateState([FromBody] WikiReviewUpdateRequest req)
        => R<WikiReviewResponse?>.Ok(await _service.UpdateStateAsync(req));
}
