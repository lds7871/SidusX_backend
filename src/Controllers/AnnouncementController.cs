using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/announcement")]
public class AnnouncementController : ControllerBase
{
    private readonly AnnouncementService _service;
    public AnnouncementController(AnnouncementService service) => _service = service;

    [HttpGet("latest")]
    [Config.BypassIpWhitelist]
    public async Task<R<AnnouncementResponse?>> GetLatest() => R<AnnouncementResponse?>.Ok(await _service.GetLatestAsync());

    [HttpGet("all")]
    public async Task<R<List<AnnouncementResponse>>> GetAll() => R<List<AnnouncementResponse>>.Ok(await _service.GetAllAsync());

    [HttpPost("create")]
    public async Task<R<long>> Create([FromBody] CreateAnnouncementRequest req)
    {
        try { return R<long>.Ok(await _service.CreateAsync(req.Content)); }
        catch (ArgumentException e) { return R<long>.Error(e.Message); }
    }
}
