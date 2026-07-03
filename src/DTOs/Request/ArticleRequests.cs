using System.Text.Json.Serialization;

namespace SidusX.DTOs.Request;

public class ArticleCreateRequest
{
    [JsonPropertyName("title")] public string Title { get; set; } = string.Empty;
    [JsonPropertyName("cover")] public string? Cover { get; set; }
    [JsonPropertyName("info")] public string? Info { get; set; }
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string? Tags { get; set; }
}

public class ArticleQueryRequest
{
    [JsonPropertyName("page")] public int Page { get; set; } = 1;
    [JsonPropertyName("page_size")] public int PageSize { get; set; } = 10;
    [JsonPropertyName("title")] public string? Title { get; set; }
    [JsonPropertyName("tags")] public string? Tags { get; set; }
    [JsonPropertyName("create_time_start")] public string? CreateTimeStart { get; set; }
    [JsonPropertyName("create_time_end")] public string? CreateTimeEnd { get; set; }
}
