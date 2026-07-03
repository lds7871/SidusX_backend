using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/nasadailyimage")]
public class NasaDailyImageController : ControllerBase
{
    private readonly NasaDailyImageService _service;
    public NasaDailyImageController(NasaDailyImageService service) => _service = service;

    [HttpGet("latest")]
    [Config.BypassIpWhitelist]
    public async Task<R<NasaDailyImageDetailResponse?>> GetLatest() => R<NasaDailyImageDetailResponse?>.Ok(await _service.GetLatestAsync());

    [HttpGet("get/{id}")]
    public async Task<R<NasaDailyImageDetailResponse?>> GetById(long id) => R<NasaDailyImageDetailResponse?>.Ok(await _service.GetByIdAsync(id));

    [HttpPost("page")]
    public async Task<R<PageResponse<NasaDailyImageListResponse>>> Page([FromBody] DTOs.Request.NasaDailyImagePageQueryRequest req)
        => R<PageResponse<NasaDailyImageListResponse>>.Ok(await _service.PageQueryAsync(req.Page, req.PageSize));

    [HttpPost("fetch")]
    public async Task<R<string>> FetchNow() { await _service.FetchAndSaveAsync(); return R<string>.Ok("NASA图片获取任务已触发"); }
}
