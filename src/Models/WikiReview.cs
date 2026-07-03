namespace SidusX.Models;

public class WikiReview
{
    public long WikireviewId { get; set; }
    public long WikiId { get; set; }
    public string Texts { get; set; } = string.Empty;
    public string[]? Tags { get; set; }
    public double Version { get; set; } = 1.0;
    public DateTime UpdateTime { get; set; }
    public string? UpdateUser { get; set; }
    public int WikiStates { get; set; } = 0;
}
