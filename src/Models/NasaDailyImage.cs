namespace SidusX.Models;

public class NasaDailyImage
{
    public long ApodId { get; set; }
    public string? Copyright { get; set; }
    public string Explanation { get; set; } = string.Empty;
    public string MediaType { get; set; } = string.Empty;
    public string Title { get; set; } = string.Empty;
    public string Url { get; set; } = string.Empty;
    public DateTime CreateTime { get; set; }
}
