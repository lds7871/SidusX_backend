using System.Net;
using System.Net.Http.Json;
using System.Text;
using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.Extensions.Options;
using SidusX.Config;

namespace SidusX.Utils;

public class DeepSeekApiClient
{
    private const string ApiUrl = "https://api.deepseek.com/chat/completions";
    private const string Model = "deepseek-v4-flash";
    private const int DefaultMaxTokens = 4096;
    private const double DefaultTemperature = 1.0;

    private readonly ILogger<DeepSeekApiClient> _logger;
    private readonly IHttpClientFactory _httpClientFactory;
    private readonly string? _apiKey;

    public DeepSeekApiClient(ILogger<DeepSeekApiClient> logger, IHttpClientFactory httpClientFactory)
    {
        _logger = logger;
        _httpClientFactory = httpClientFactory;
        _apiKey = Environment.GetEnvironmentVariable("DEEPSEEK_API_KEY");
        if (string.IsNullOrEmpty(_apiKey))
            _logger.LogWarning("环境变量 DEEPSEEK_API_KEY 未设置");
    }

    public async Task<string> ChatWithDefaultAsync(string userQuestion, string systemPrompt)
        => await ChatAsync(userQuestion, systemPrompt, DefaultTemperature, DefaultMaxTokens);

    public async Task<string> ChatWithCustomParamsAsync(string userQuestion, string systemPrompt,
        double? temperature, int? maxTokens)
        => await ChatAsync(userQuestion, systemPrompt,
            temperature ?? DefaultTemperature, maxTokens ?? DefaultMaxTokens);

    private async Task<string> ChatAsync(string userQuestion, string systemPrompt, double temperature, int maxTokens)
    {
        if (string.IsNullOrEmpty(_apiKey))
            throw new InvalidOperationException("DEEPSEEK_API_KEY 环境变量未设置");

        var body = new
        {
            model = Model,
            messages = new[]
            {
                new { role = "system", content = systemPrompt },
                new { role = "user", content = userQuestion }
            },
            temperature,
            max_tokens = maxTokens,
            top_p = 1.0,
            stream = false
        };

        var json = JsonSerializer.Serialize(body);
        var client = _httpClientFactory.CreateClient("deepseek");
        using var request = new HttpRequestMessage(HttpMethod.Post, ApiUrl)
        {
            Content = new StringContent(json, Encoding.UTF8, "application/json"),
            Headers = { { "Authorization", "Bearer " + _apiKey } }
        };

        var response = await client.SendAsync(request);
        var responseJson = await response.Content.ReadAsStringAsync();

        using var doc = JsonDocument.Parse(responseJson);
        var root = doc.RootElement;
        return root.GetProperty("choices")[0].GetProperty("message").GetProperty("content").GetString()
               ?? throw new InvalidOperationException("API响应格式错误或无有效内容");
    }
}
