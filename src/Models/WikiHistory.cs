namespace SidusX.Models;

public class WikiHistory
{
    public long HistoryId { get; set; }
    public long WikiId { get; set; }
    public string KeyName { get; set; } = string.Empty;
    public string Texts { get; set; } = string.Empty;
    public string[]? Tags { get; set; }
    public double Version { get; set; }
    public DateTime CreateTime { get; set; }
    public string? CreateUser { get; set; }
    public DateTime UpdateTime { get; set; }
    public string? UpdateUser { get; set; }
    public DateTime BackupTime { get; set; } = DateTime.Now;
}
