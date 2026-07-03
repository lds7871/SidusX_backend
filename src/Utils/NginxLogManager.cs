namespace SidusX.Utils;

/// <summary>Nginx日志管理工具</summary>
public static class NginxLogManager
{
    private static readonly string LogBasePath =
        @"C:\Users\Administrator\Desktop\ServerSync\Nginx日志";

    public static (bool Exists, string Path, List<string> Lines, int TotalLines) ReadLog(int limit)
    {
        var yesterday = DateTime.Now.Date.AddDays(-1);
        var dateStr = yesterday.ToString("yyyy-MM-dd");
        var logPath = System.IO.Path.Combine(LogBasePath, $"access({dateStr}).log");

        if (!File.Exists(logPath))
            return (false, logPath, new List<string>(), 0);

        var allLines = File.ReadAllLines(logPath).ToList();
        var startIndex = Math.Max(0, allLines.Count - limit);
        var lines = allLines.Skip(startIndex).ToList();
        return (true, logPath, lines, allLines.Count);
    }
}
