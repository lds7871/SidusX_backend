using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/wikinew")]
public class WikiNewController : ControllerBase
{
    private readonly WikiNewService _service;
    public WikiNewController(WikiNewService service) => _service = service;

    [HttpPost("create")]
    public async Task<R<WikiNewResponse>> Create([FromBody] WikiNewCreateRequest req)
    {
        try { return R<WikiNewResponse>.Ok(await _service.CreateAsync(req)); }
        catch (ArgumentException e) { return R<WikiNewResponse>.Error(e.Message); }
    }

    [HttpGet("get/{id}")]
    public async Task<R<WikiNewResponse?>> GetById(long id) => R<WikiNewResponse?>.Ok(await _service.GetByIdAsync(id));

    [HttpPost("page")]
    public async Task<R<PageResponse<WikiNewListResponse>>> Page([FromBody] WikiNewPageQueryRequest req)
        => R<PageResponse<WikiNewListResponse>>.Ok(await _service.PageQueryAsync(req));

    [HttpPost("review")]
    public async Task<R<WikiNewReviewResponse?>> Review([FromBody] WikiNewReviewRequest req)
    {
        try { return R<WikiNewReviewResponse?>.Ok(await _service.ReviewAsync(req)); }
        catch (ArgumentException e) { return R<WikiNewReviewResponse?>.Error(e.Message); }
    }
}
