using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class FalconStatsRepository
{
    private readonly NpgsqlDataSource _ds;
    public FalconStatsRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task InsertAsync(FalconStats s)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        await conn.ExecuteAsync(
            "INSERT INTO falcon_stats (document_id, total_launches, total_landings, total_reflights, created_at) VALUES (@documentId, @totalLaunches, @totalLandings, @totalReflights, @createdAt)",
            new { documentId = s.DocumentId, totalLaunches = s.TotalLaunches, totalLandings = s.TotalLandings, totalReflights = s.TotalReflights, createdAt = s.CreatedAt });
    }

    public async Task<FalconStats?> GetLatestAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<FalconStats>("SELECT * FROM falcon_stats ORDER BY created_at DESC LIMIT 1");
    }
}
