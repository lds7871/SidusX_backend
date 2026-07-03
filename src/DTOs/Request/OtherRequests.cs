using System.Text.Json.Serialization;

namespace SidusX.DTOs.Request;

public class ApiLogQueryRequest
{
    [JsonPropertyName("states")] public int? States { get; set; }
    [JsonPropertyName("start_time")] public string? StartTime { get; set; }
    [JsonPropertyName("end_time")] public string? EndTime { get; set; }
}

public class NasaDailyImagePageQueryRequest
{
    [JsonPropertyName("page")] public int Page { get; set; } = 1;
    [JsonPropertyName("page_size")] public int PageSize { get; set; } = 10;
}

public class MsShipCreateRequest
{
    [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
}

public class DeepSeekChatRequest
{
    [JsonPropertyName("user_question")] public string UserQuestion { get; set; } = string.Empty;
    [JsonPropertyName("system_prompt")] public string SystemPrompt { get; set; } = string.Empty;
    [JsonPropertyName("temperature")] public double? Temperature { get; set; }
    [JsonPropertyName("max_tokens")] public int? MaxTokens { get; set; }
}

public class DeepSeekChatCustomRequest
{
    [JsonPropertyName("user_question")] public string UserQuestion { get; set; } = string.Empty;
    [JsonPropertyName("system_prompt")] public string SystemPrompt { get; set; } = string.Empty;
    [JsonPropertyName("temperature")] public double? Temperature { get; set; }
    [JsonPropertyName("max_tokens")] public int? MaxTokens { get; set; }
}

public class CreateAnnouncementRequest
{
    [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
}

public class CreateMsShipRequest
{
    [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
}

// Unified chat request with optional params (replaces DeepSeekChatCustomRequest inline)
