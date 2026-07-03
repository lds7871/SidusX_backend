using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/msship")]
public class MsShipController : ControllerBase
{
    private readonly MsShipService _service;
    public MsShipController(MsShipService service) => _service = service;

    [HttpPost("create")]
    public async Task<R<long>> Create([FromBody] CreateMsShipRequest req) => R<long>.Ok(await _service.CreateAsync(req.Content));

    [HttpGet("get/{id}")]
    public async Task<R<MsShipResponse?>> GetById(long id) => R<MsShipResponse?>.Ok(await _service.GetByIdAsync(id));

    [HttpGet("all")]
    [Config.BypassIpWhitelist]
    public async Task<R<List<MsShipResponse>>> GetAll() => R<List<MsShipResponse>>.Ok(await _service.GetAllAsync());

    [HttpPut("update/{id}")]
    public async Task<R<bool>> Update(long id, [FromBody] CreateMsShipRequest req) => R<bool>.Ok(await _service.UpdateAsync(id, req.Content));

    [HttpDelete("delete/{id}")]
    public async Task<R<bool>> Delete(long id) => R<bool>.Ok(await _service.DeleteAsync(id));
}
