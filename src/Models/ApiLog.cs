namespace SidusX.Models;

public class ApiLog
{
    public int Id { get; set; }
    public string Ip { get; set; } = string.Empty;
    public string Api { get; set; } = string.Empty;
    public int States { get; set; }
    public DateTime CreateTime { get; set; }
}
