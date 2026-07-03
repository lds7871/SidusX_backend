using Microsoft.AspNetCore.Mvc;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Utils;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/deepseek")]
public class DeepSeekController : ControllerBase
{
    private readonly DeepSeekApiClient _client;
    public DeepSeekController(DeepSeekApiClient client) => _client = client;

    [HttpPost("chat")]
    public async Task<R<DeepSeekChatResponse>> Chat([FromBody] DeepSeekChatRequest req)
    {
        try
        {
            var result = req.Temperature.HasValue || req.MaxTokens.HasValue
                ? await _client.ChatWithCustomParamsAsync(req.UserQuestion, req.SystemPrompt, req.Temperature, req.MaxTokens)
                : await _client.ChatWithDefaultAsync(req.UserQuestion, req.SystemPrompt);
            return R<DeepSeekChatResponse>.Ok(new DeepSeekChatResponse { Content = result });
        }
        catch (Exception e) { return R<DeepSeekChatResponse>.Error(e.Message); }
    }
}
