using System.Diagnostics;
using Dapper;
using Microsoft.AspNetCore.Mvc;
using Npgsql;
using SidusX.DTOs.Response;
using SidusX.Utils;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/serverinfo")]
public class ServerInfoController : ControllerBase
{
    private readonly NpgsqlDataSource _ds;
    private readonly ILogger<ServerInfoController> _logger;

    public ServerInfoController(NpgsqlDataSource ds, ILogger<ServerInfoController> logger)
    {
        _ds = ds; _logger = logger;
    }

    [HttpGet("runtime")]
    public R<RuntimeInfoResponse> GetRuntimeInfo()
    {
        var proc = Process.GetCurrentProcess();
        return R<RuntimeInfoResponse>.Ok(new RuntimeInfoResponse
        {
            RuntimeVersion = Environment.Version.ToString(),
            OsDescription = System.Runtime.InteropServices.RuntimeInformation.OSDescription,
            ProcessorCount = Environment.ProcessorCount,
            WorkingSet = proc.WorkingSet64,
            GcTotalMemory = GC.GetTotalMemory(false),
            ThreadCount = proc.Threads.Count,
            Uptime = (DateTime.Now - proc.StartTime).ToString(@"d\.hh\:mm\:ss"),
            StartTime = proc.StartTime.ToString("yyyy-MM-dd HH:mm:ss"),
            CurrentTime = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss")
        });
    }

    [HttpGet("apilogs")]
    public async Task<R<object>> GetApiLogs([FromQuery] int limit = 100)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var rows = await conn.QueryAsync("SELECT * FROM api_log ORDER BY log_time DESC LIMIT @limit", new { limit });
        return R<object>.Ok(rows);
    }

    [HttpGet("tablecounts")]
    public async Task<R<Dictionary<string, long>>> GetTableCounts()
    {
        var tables = new[] { "users", "wiki", "wiki_history", "wiki_review", "wiki_new", "wiki_comment",
            "article", "announcement", "nasa_daily_image", "falcon_stats", "ms_ship", "recent_launch" };
        var result = new Dictionary<string, long>();
        await using var conn = await _ds.OpenConnectionAsync();
        foreach (var table in tables)
        {
            try { result[table] = await conn.ExecuteScalarAsync<long>($"SELECT COUNT(*) FROM {table}"); }
            catch { result[table] = -1; }
        }
        return R<Dictionary<string, long>>.Ok(result);
    }

    [HttpGet("nginxlog")]
    public R<NginxLogResponse> GetNginxLog([FromQuery] int limit = 100)
    {
        var (exists, path, lines, total) = NginxLogManager.ReadLog(limit);
        return R<NginxLogResponse>.Ok(new NginxLogResponse { Exists = exists, LogPath = path, Lines = lines, TotalLines = total });
    }
}
