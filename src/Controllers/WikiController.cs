using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/wiki")]
public class WikiController : ControllerBase
{
    private readonly WikiService _service;
    public WikiController(WikiService service) => _service = service;

    [HttpPost("create")]
    public async Task<R<WikiResponse>> Create([FromBody] WikiCreateRequest req)
    {
        try { return R<WikiResponse>.Ok(await _service.CreateWikiAsync(req)); }
        catch (ArgumentException e) { return R<WikiResponse>.Error(e.Message); }
    }

    [HttpDelete("delete/{wikiId}")]
    public async Task<R<bool>> Delete(long wikiId) => R<bool>.Ok(await _service.DeleteWikiAsync(wikiId));

    [HttpGet("get/{wikiId}")]
    public async Task<R<WikiResponse?>> GetById(long wikiId) => R<WikiResponse?>.Ok(await _service.GetByIdAsync(wikiId));

    [HttpGet("getbykeyname/{keyName}")]
    public async Task<R<WikiResponse?>> GetByKeyName(string keyName) => R<WikiResponse?>.Ok(await _service.GetByKeyNameAsync(keyName));

    [HttpGet("checkkey/{keyName}")]
    public async Task<R<bool>> CheckKeyName(string keyName) => R<bool>.Ok(await _service.IsKeyNameExistsAsync(keyName));

    [HttpPost("update")]
    public async Task<R<WikiResponse?>> Update([FromBody] WikiUpdateRequest req)
    {
        try { return R<WikiResponse?>.Ok(await _service.UpdateWikiAsync(req)); }
        catch (ArgumentException e) { return R<WikiResponse?>.Error(e.Message); }
    }

    [HttpPost("page")]
    public async Task<R<PageResponse<WikiResponse>>> Page([FromBody] WikiPageQueryRequest req)
        => R<PageResponse<WikiResponse>>.Ok(await _service.PageQueryAsync(req));

    [HttpGet("latest")]
    public async Task<R<List<LatestWikiSummaryResponse>>> Latest([FromQuery] int limit = 10)
        => R<List<LatestWikiSummaryResponse>>.Ok(await _service.GetLatestAsync(limit));
}
