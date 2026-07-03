namespace SidusX.Models;

public class WikiNew
{
    public long WikinewId { get; set; }
    public string KeyName { get; set; } = string.Empty;
    public string Texts { get; set; } = string.Empty;
    public string[]? Tags { get; set; }
    public double Version { get; set; } = 1.0;
    public DateTime CreateTime { get; set; }
    public string? CreateUser { get; set; }
    public DateTime UpdateTime { get; set; }
    public string? UpdateUser { get; set; }
    public int WikiStates { get; set; } = 0;
}
