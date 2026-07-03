using System.Text.Json.Serialization;

namespace SidusX.DTOs.Request;

public class WikiCreateRequest
{
    [JsonPropertyName("key_name")] public string KeyName { get; set; } = string.Empty;
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("create_user")] public string? CreateUser { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
}

public class WikiUpdateRequest
{
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("texts")] public string? Texts { get; set; }
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
}

public class WikiPageQueryRequest
{
    [JsonPropertyName("page")] public int Page { get; set; } = 1;
    [JsonPropertyName("page_size")] public int PageSize { get; set; } = 10;
    [JsonPropertyName("key_name")] public string? KeyName { get; set; }
    [JsonPropertyName("tags")] public string? Tags { get; set; }
    [JsonPropertyName("create_time_start")] public string? CreateTimeStart { get; set; }
    [JsonPropertyName("create_time_end")] public string? CreateTimeEnd { get; set; }
}

public class WikiCommentCreateRequest
{
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("user_id")] public long UserId { get; set; }
    [JsonPropertyName("text")] public string Text { get; set; } = string.Empty;
}

public class WikiCommentByWikiIdRequest
{
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("page")] public int Page { get; set; } = 1;
    [JsonPropertyName("page_size")] public int PageSize { get; set; } = 10;
}

public class WikiNewCreateRequest
{
    [JsonPropertyName("key_name")] public string KeyName { get; set; } = string.Empty;
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("create_user")] public string? CreateUser { get; set; }
}

public class WikiNewPageQueryRequest
{
    [JsonPropertyName("page")] public int Page { get; set; } = 1;
    [JsonPropertyName("page_size")] public int PageSize { get; set; } = 10;
    [JsonPropertyName("key_name")] public string? KeyName { get; set; }
    [JsonPropertyName("wiki_states")] public int? WikiStates { get; set; }
}

public class WikiNewReviewRequest
{
    [JsonPropertyName("wikinew_id")] public long WikinewId { get; set; }
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
}

public class WikiReviewCreateRequest
{
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
}

public class WikiReviewPageQueryRequest
{
    [JsonPropertyName("page")] public int Page { get; set; } = 1;
    [JsonPropertyName("page_size")] public int PageSize { get; set; } = 10;
    [JsonPropertyName("wiki_id")] public long? WikiId { get; set; }
    [JsonPropertyName("wiki_states")] public int? WikiStates { get; set; }
}

public class WikiReviewUpdateRequest
{
    [JsonPropertyName("wikireview_id")] public long WikireviewId { get; set; }
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
}
