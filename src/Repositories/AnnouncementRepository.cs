using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class AnnouncementRepository
{
    private readonly NpgsqlDataSource _ds;
    public AnnouncementRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<Announcement?> GetLatestAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<Announcement>(
            "SELECT ann_id, content, create_time FROM announcement ORDER BY create_time DESC LIMIT 1");
    }

    public async Task<List<Announcement>> GetAllAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return (await conn.QueryAsync<Announcement>("SELECT ann_id, content, create_time FROM announcement ORDER BY create_time DESC")).ToList();
    }

    public async Task<long> InsertAsync(string content)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<long>(
            "INSERT INTO announcement (content, create_time) VALUES (@content, @createTime) RETURNING ann_id",
            new { content, createTime = DateTime.Now });
    }
}
