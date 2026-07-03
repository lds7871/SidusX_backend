using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class NasaDailyImageRepository
{
    private readonly NpgsqlDataSource _ds;
    public NasaDailyImageRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(NasaDailyImage img)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<long>(
            "INSERT INTO nasa_daily_image (copyright, explanation, media_type, title, url, create_time) VALUES (@copyright, @explanation, @mediaType, @title, @url, @createTime) RETURNING apod_id",
            new { copyright = img.Copyright, explanation = img.Explanation, mediaType = img.MediaType, title = img.Title, url = img.Url, createTime = img.CreateTime });
    }

    public async Task<NasaDailyImage?> GetByIdAsync(long apodId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<NasaDailyImage>("SELECT * FROM nasa_daily_image WHERE apod_id = @apodId", new { apodId });
    }

    public async Task<NasaDailyImage?> GetLatestAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<NasaDailyImage>("SELECT * FROM nasa_daily_image ORDER BY create_time DESC LIMIT 1");
    }

    public async Task<(List<NasaDailyImage> Items, long Total)> PageQueryAsync(int page, int pageSize)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var total = await conn.QuerySingleAsync<long>("SELECT COUNT(*) FROM nasa_daily_image");
        var offset = (page - 1) * pageSize;
        var items = (await conn.QueryAsync<NasaDailyImage>(
            "SELECT * FROM nasa_daily_image ORDER BY create_time DESC LIMIT @limit OFFSET @offset",
            new { limit = pageSize, offset })).ToList();
        return (items, total);
    }

    public async Task<bool> IsTodayImageExistsAsync()
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<int>(
            "SELECT COUNT(*) FROM nasa_daily_image WHERE create_time >= @today",
            new { today = DateTime.Today }) > 0;
    }
}
