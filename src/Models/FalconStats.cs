namespace SidusX.Models;

public class FalconStats
{
    public long FalconId { get; set; }
    public string DocumentId { get; set; } = string.Empty;
    public int TotalLaunches { get; set; }
    public int TotalLandings { get; set; }
    public int TotalReflights { get; set; }
    public DateTime CreatedAt { get; set; }
}
