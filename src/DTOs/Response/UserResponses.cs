using System.Text.Json.Serialization;

namespace SidusX.DTOs.Response;

public class UserInfoResponse
{
    [JsonPropertyName("user_id")] public long UserId { get; set; }
    [JsonPropertyName("name")] public string Name { get; set; } = string.Empty;
    [JsonPropertyName("cover")] public string? Cover { get; set; }
    [JsonPropertyName("phone")] public string? Phone { get; set; }
    [JsonPropertyName("mail")] public string? Mail { get; set; }
    [JsonPropertyName("place")] public string? Place { get; set; }
    [JsonPropertyName("achievement_json")] public string? AchievementJson { get; set; }
    [JsonPropertyName("expired_time")] public DateTime? ExpiredTime { get; set; }
}

public class UserResultResponse
{
    [JsonPropertyName("code")] public int Code { get; set; }
    [JsonPropertyName("message")] public string? Message { get; set; }
    [JsonPropertyName("data")] public object? Data { get; set; }
    [JsonPropertyName("timestamp")] public long Timestamp { get; set; } = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
}

public class GameAchievementResponse
{
    [JsonPropertyName("user_id")] public long UserId { get; set; }
    [JsonPropertyName("achievements")] public object? Achievements { get; set; }
}
