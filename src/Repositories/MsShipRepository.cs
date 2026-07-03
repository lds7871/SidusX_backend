using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class MsShipRepository
{
    private readonly NpgsqlDataSource _ds;
    public MsShipRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(string content)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<long>(
            "INSERT INTO ms_ship (content) VALUES (@content::jsonb) RETURNING ms_id", new { content });
    }

    public async Task<MsShip?> GetByIdAsync(long msId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<MsShip>(
            "SELECT ms_id, content::text AS content FROM ms_ship WHERE ms_id = @msId", new { msId });
    }

    public async Task<List<MsShip>> GetAllAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return (await conn.QueryAsync<MsShip>("SELECT ms_id, content::text AS content FROM ms_ship ORDER BY ms_id")).ToList();
    }

    public async Task<bool> UpdateAsync(long msId, string content)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.ExecuteAsync("UPDATE ms_ship SET content = @content::jsonb WHERE ms_id = @msId", new { msId, content }) > 0;
    }

    public async Task<bool> DeleteAsync(long msId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.ExecuteAsync("DELETE FROM ms_ship WHERE ms_id = @msId", new { msId }) > 0;
    }
}
