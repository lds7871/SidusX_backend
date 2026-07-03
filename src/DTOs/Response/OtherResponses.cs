using System.Text.Json.Serialization;

namespace SidusX.DTOs.Response;

public class ArticleResponse
{
    [JsonPropertyName("article_id")] public long ArticleId { get; set; }
    [JsonPropertyName("title")] public string Title { get; set; } = string.Empty;
    [JsonPropertyName("cover")] public string? Cover { get; set; }
    [JsonPropertyName("info")] public string? Info { get; set; }
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string? Tags { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
}

public class ArticleListResponse
{
    [JsonPropertyName("article_id")] public long ArticleId { get; set; }
    [JsonPropertyName("title")] public string Title { get; set; } = string.Empty;
    [JsonPropertyName("cover")] public string? Cover { get; set; }
    [JsonPropertyName("info")] public string? Info { get; set; }
    [JsonPropertyName("tags")] public string? Tags { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
}

public class ArticleLatestResponse
{
    [JsonPropertyName("article_id")] public long ArticleId { get; set; }
    [JsonPropertyName("title")] public string Title { get; set; } = string.Empty;
    [JsonPropertyName("cover")] public string? Cover { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
}

public class ArticleResultResponse
{
    [JsonPropertyName("code")] public int Code { get; set; }
    [JsonPropertyName("message")] public string? Message { get; set; }
    [JsonPropertyName("data")] public object? Data { get; set; }
    [JsonPropertyName("timestamp")] public long Timestamp { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}

public class ArticlePageResponse
{
    [JsonPropertyName("total")] public long Total { get; set; }
    [JsonPropertyName("page")] public int Page { get; set; }
    [JsonPropertyName("page_size")] public int PageSize { get; set; }
    [JsonPropertyName("data")] public List<ArticleListResponse> Data { get; set; } = new();
}

public class AnnouncementResponse
{
    [JsonPropertyName("ann_id")] public long AnnId { get; set; }
    [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
}

public class ApiLogResponse
{
    [JsonPropertyName("id")] public int Id { get; set; }
    [JsonPropertyName("ip")] public string Ip { get; set; } = string.Empty;
    [JsonPropertyName("api")] public string Api { get; set; } = string.Empty;
    [JsonPropertyName("states")] public int States { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
}

public class ApiLogSimpleResultResponse
{
    [JsonPropertyName("code")] public int Code { get; set; }
    [JsonPropertyName("message")] public string? Message { get; set; }
    [JsonPropertyName("data")] public List<ApiLogResponse>? Data { get; set; }
    [JsonPropertyName("timestamp")] public long Timestamp { get; set; }
}

public class NasaDailyImageDetailResponse
{
    [JsonPropertyName("apod_id")] public long ApodId { get; set; }
    [JsonPropertyName("copyright")] public string? Copyright { get; set; }
    [JsonPropertyName("explanation")] public string Explanation { get; set; } = string.Empty;
    [JsonPropertyName("media_type")] public string MediaType { get; set; } = string.Empty;
    [JsonPropertyName("title")] public string Title { get; set; } = string.Empty;
    [JsonPropertyName("url")] public string Url { get; set; } = string.Empty;
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
}

public class NasaDailyImageListResponse
{
    [JsonPropertyName("apod_id")] public long ApodId { get; set; }
    [JsonPropertyName("title")] public string Title { get; set; } = string.Empty;
    [JsonPropertyName("url")] public string Url { get; set; } = string.Empty;
    [JsonPropertyName("media_type")] public string MediaType { get; set; } = string.Empty;
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
}

public class MsShipResponse
{
    [JsonPropertyName("ms_id")] public long MsId { get; set; }
    [JsonPropertyName("content")] public string Content { get; set; } = string.Empty;
}

public class RecentLaunchDataResponse
{
    [JsonPropertyName("id")] public long Id { get; set; }
    [JsonPropertyName("data")] public string Data { get; set; } = string.Empty;
    [JsonPropertyName("get_time")] public DateTime GetTime { get; set; }
}

public class DeepSeekChatResponse
{
    [JsonPropertyName("success")] public bool Success { get; set; }
    [JsonPropertyName("content")] public string? Content { get; set; }
    [JsonPropertyName("message")] public string? Message { get; set; }
    [JsonPropertyName("ai_response")] public string? AiResponse { get; set; }
    [JsonPropertyName("error_message")] public string? ErrorMessage { get; set; }
    [JsonPropertyName("timestamp")] public long Timestamp { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}
