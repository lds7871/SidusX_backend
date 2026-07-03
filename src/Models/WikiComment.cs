namespace SidusX.Models;

public class WikiComment
{
    public long ReplyId { get; set; }
    public long WikiId { get; set; }
    public long UserId { get; set; }
    public string Text { get; set; } = string.Empty;
    public int Likes { get; set; } = 0;
    public DateTime CreateTime { get; set; }
}
