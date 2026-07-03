using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/article")]
public class ArticleController : ControllerBase
{
    private readonly ArticleService _service;
    public ArticleController(ArticleService service) => _service = service;

    [HttpPost("create")]
    public async Task<R<ArticleResponse>> Create([FromBody] ArticleCreateRequest req)
    {
        try { return R<ArticleResponse>.Ok(await _service.CreateAsync(req)); }
        catch (ArgumentException e) { return R<ArticleResponse>.Error(e.Message); }
    }

    [HttpDelete("delete/{id}")]
    public async Task<R<bool>> Delete(long id) => R<bool>.Ok(await _service.DeleteAsync(id));

    [HttpGet("get/{id}")]
    public async Task<R<ArticleResponse?>> GetById(long id) => R<ArticleResponse?>.Ok(await _service.GetByIdAsync(id));

    [HttpPut("update/{id}")]
    public async Task<R<ArticleResponse?>> Update(long id, [FromBody] ArticleCreateRequest req)
        => R<ArticleResponse?>.Ok(await _service.UpdateAsync(id, req));

    [HttpPost("page")]
    public async Task<R<ArticlePageResponse>> Page([FromBody] ArticleQueryRequest req)
        => R<ArticlePageResponse>.Ok(await _service.PageQueryAsync(req));

    [HttpGet("latest")]
    public async Task<R<List<ArticleLatestResponse>>> Latest([FromQuery] int limit = 5)
        => R<List<ArticleLatestResponse>>.Ok(await _service.GetLatestAsync(limit));
}
