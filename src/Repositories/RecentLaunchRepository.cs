using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class RecentLaunchRepository
{
    private readonly NpgsqlDataSource _ds;
    public RecentLaunchRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task InsertAsync(string data)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        await conn.ExecuteAsync(
            "INSERT INTO recent_launch (data, get_time) VALUES (@data::jsonb, @getTime)",
            new { data, getTime = DateTime.Now });
    }

    public async Task<RecentLaunch?> GetLatestAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<RecentLaunch>(
            "SELECT id, data::text AS data, get_time FROM recent_launch ORDER BY get_time DESC LIMIT 1");
    }
}
