using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/recentlaunch")]
public class RecentLaunchController : ControllerBase
{
    private readonly RecentLaunchService _service;
    public RecentLaunchController(RecentLaunchService service) => _service = service;

    [HttpGet("latest")]
    [Config.BypassIpWhitelist]
    public async Task<R<RecentLaunchDataResponse?>> GetLatest() => R<RecentLaunchDataResponse?>.Ok(await _service.GetLatestAsync());

    [HttpPost("fetch")]
    public async Task<R<string>> FetchNow() { await _service.FetchAndSaveAsync(); return R<string>.Ok("最近发射数据获取任务已触发"); }
}
