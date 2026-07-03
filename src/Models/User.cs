namespace SidusX.Models;

public class User
{
    public long UserId { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Cover { get; set; }
    public string? Phone { get; set; }
    public string? Mail { get; set; }
    public string PasswordHash { get; set; } = string.Empty;
    public string? Place { get; set; }
    public string AchievementJson { get; set; } = "{}";
    public DateTime? ExpiredTime { get; set; }
}
