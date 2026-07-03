using System.Text.Json.Serialization;

namespace SidusX.DTOs.Request;

public class UserLoginRequest
{
    [JsonPropertyName("mail")] public string? Mail { get; set; }
    [JsonPropertyName("phone")] public string? Phone { get; set; }
    [JsonPropertyName("password")] public string Password { get; set; } = string.Empty;
}

public class UserRegisterSendCodeRequest
{
    [JsonPropertyName("name")] public string Name { get; set; } = string.Empty;
    [JsonPropertyName("mail")] public string Mail { get; set; } = string.Empty;
    [JsonPropertyName("phone")] public string? Phone { get; set; }
    [JsonPropertyName("password")] public string Password { get; set; } = string.Empty;
    [JsonPropertyName("place")] public string? Place { get; set; }
}

public class UserRegisterConfirmRequest
{
    [JsonPropertyName("mail")] public string Mail { get; set; } = string.Empty;
    [JsonPropertyName("verify_code")] public string VerifyCode { get; set; } = string.Empty;
}

public class ChangePasswordSendCodeRequest
{
    [JsonPropertyName("mail")] public string Mail { get; set; } = string.Empty;
}

public class ChangePasswordConfirmRequest
{
    [JsonPropertyName("mail")] public string Mail { get; set; } = string.Empty;
    [JsonPropertyName("verify_code")] public string VerifyCode { get; set; } = string.Empty;
    [JsonPropertyName("new_password")] public string NewPassword { get; set; } = string.Empty;
}

public class UpdateCoverRequest
{
    [JsonPropertyName("user_id")] public long UserId { get; set; }
    [JsonPropertyName("cover")] public string Cover { get; set; } = string.Empty;
}

public class UpdatePlaceRequest
{
    [JsonPropertyName("user_id")] public long UserId { get; set; }
    [JsonPropertyName("place")] public string Place { get; set; } = string.Empty;
}

public class UpdateGameAchievementRequest
{
    [JsonPropertyName("userid")] public long Userid { get; set; }
    [JsonPropertyName("gamename")] public string Gamename { get; set; } = string.Empty;
    [JsonPropertyName("gamescore")] public int Gamescore { get; set; }
}
