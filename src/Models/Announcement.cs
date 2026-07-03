namespace SidusX.Models;

public class Announcement
{
    public long AnnId { get; set; }
    public string Content { get; set; } = string.Empty;
    public DateTime CreateTime { get; set; }
}
