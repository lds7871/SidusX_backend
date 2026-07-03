using System.Text.Json.Serialization;

namespace SidusX.DTOs.Response;

public class WikiResponse
{
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("key_name")] public string KeyName { get; set; } = string.Empty;
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("version")] public double Version { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
    [JsonPropertyName("create_user")] public string? CreateUser { get; set; }
    [JsonPropertyName("create_user_name")] public string? CreateUserName { get; set; }
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
    [JsonPropertyName("update_user_name")] public string? UpdateUserName { get; set; }
}

public class LatestWikiSummaryResponse
{
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("key_name")] public string KeyName { get; set; } = string.Empty;
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
}

public class WikiCommentResponse
{
    [JsonPropertyName("reply_id")] public long ReplyId { get; set; }
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("user_id")] public long UserId { get; set; }
    [JsonPropertyName("user_name")] public string? UserName { get; set; }
    [JsonPropertyName("text")] public string Text { get; set; } = string.Empty;
    [JsonPropertyName("likes")] public int Likes { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
}

public class WikiNewResponse
{
    [JsonPropertyName("wikinew_id")] public long WikinewId { get; set; }
    [JsonPropertyName("key_name")] public string KeyName { get; set; } = string.Empty;
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("version")] public double Version { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
    [JsonPropertyName("create_user")] public string? CreateUser { get; set; }
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
}

public class WikiNewListResponse
{
    [JsonPropertyName("wikinew_id")] public long WikinewId { get; set; }
    [JsonPropertyName("key_name")] public string KeyName { get; set; } = string.Empty;
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
    [JsonPropertyName("create_time")] public DateTime CreateTime { get; set; }
    [JsonPropertyName("create_user")] public string? CreateUser { get; set; }
}

public class WikiNewReviewResponse
{
    [JsonPropertyName("wikinew_id")] public long WikinewId { get; set; }
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
    [JsonPropertyName("message")] public string Message { get; set; } = string.Empty;
}

public class WikiReviewResponse
{
    [JsonPropertyName("wikireview_id")] public long WikireviewId { get; set; }
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("texts")] public string Texts { get; set; } = string.Empty;
    [JsonPropertyName("tags")] public string[]? Tags { get; set; }
    [JsonPropertyName("version")] public double Version { get; set; }
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
}

public class WikiReviewListResponse
{
    [JsonPropertyName("wikireview_id")] public long WikireviewId { get; set; }
    [JsonPropertyName("wiki_id")] public long WikiId { get; set; }
    [JsonPropertyName("wiki_states")] public int WikiStates { get; set; }
    [JsonPropertyName("update_time")] public DateTime UpdateTime { get; set; }
    [JsonPropertyName("update_user")] public string? UpdateUser { get; set; }
}
