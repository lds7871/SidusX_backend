namespace SidusX.Models;

public class Article
{
    public long ArticleId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string? Cover { get; set; }
    public string? Info { get; set; }
    public string Texts { get; set; } = string.Empty;
    public string? Tags { get; set; }
    public DateTime CreateTime { get; set; }
    public DateTime UpdateTime { get; set; }
}
