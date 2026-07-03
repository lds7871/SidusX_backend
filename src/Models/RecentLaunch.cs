namespace SidusX.Models;

public class RecentLaunch
{
    public long Id { get; set; }
    public string Data { get; set; } = string.Empty;
    public DateTime GetTime { get; set; }
}
