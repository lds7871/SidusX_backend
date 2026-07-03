using Dapper;
using Npgsql;
using SidusX.Models;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;

namespace SidusX.Repositories;

public class WikiRepository
{
    private readonly NpgsqlDataSource _ds;
    public WikiRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(Wiki wiki)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = @"INSERT INTO wiki (key_name, texts, tags, version, create_time, create_user, update_time, update_user)
                    VALUES (@keyName, @texts, @tags, @version, @createTime, @createUser, @updateTime, @updateUser)
                    RETURNING wiki_id";
        return await conn.QuerySingleAsync<long>(sql, new
        {
            keyName = wiki.KeyName, texts = wiki.Texts, tags = wiki.Tags,
            version = wiki.Version, createTime = wiki.CreateTime, createUser = wiki.CreateUser,
            updateTime = wiki.UpdateTime, updateUser = wiki.UpdateUser
        });
    }

    public async Task<bool> DeleteAsync(long wikiId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.ExecuteAsync("DELETE FROM wiki WHERE wiki_id = @wikiId", new { wikiId }) > 0;
    }

    public async Task<Wiki?> GetByIdAsync(long wikiId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = "SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user FROM wiki WHERE wiki_id = @wikiId";
        return await conn.QueryFirstOrDefaultAsync<Wiki>(sql, new { wikiId });
    }

    public async Task<Wiki?> GetByKeyNameAsync(string keyName)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = "SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user FROM wiki WHERE key_name = @keyName";
        return await conn.QueryFirstOrDefaultAsync<Wiki>(sql, new { keyName });
    }

    public async Task<bool> IsKeyNameExistsAsync(string keyName)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<int>("SELECT COUNT(*) FROM wiki WHERE key_name = @keyName", new { keyName }) > 0;
    }

    public async Task UpdateAsync(long wikiId, string texts, string[]? tags, string? updateUser)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = @"UPDATE wiki SET texts = @texts, tags = @tags, update_time = @updateTime, update_user = @updateUser,
                    version = ROUND((version + 0.01)::numeric, 2) WHERE wiki_id = @wikiId";
        await conn.ExecuteAsync(sql, new { wikiId, texts, tags, updateTime = DateTime.Now, updateUser });
    }

    public async Task<(List<Wiki> Items, long Total)> PageQueryAsync(WikiPageQueryRequest req)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var where = BuildWhereClause(req, out var parameters);
        var countSql = $"SELECT COUNT(*) FROM wiki {where}";
        var total = await conn.QuerySingleAsync<long>(countSql, parameters);
        var offset = (req.Page - 1) * req.PageSize;
        var sql = $@"SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user
                     FROM wiki {where} ORDER BY update_time DESC LIMIT @limit OFFSET @offset";
        parameters.Add("limit", req.PageSize);
        parameters.Add("offset", offset);
        var items = (await conn.QueryAsync<Wiki>(sql, parameters)).ToList();
        return (items, total);
    }

    public async Task<List<Wiki>> GetLatestAsync(int limit)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = "SELECT wiki_id, key_name, update_time, update_user FROM wiki ORDER BY update_time DESC LIMIT @limit";
        return (await conn.QueryAsync<Wiki>(sql, new { limit })).ToList();
    }

    private static string BuildWhereClause(WikiPageQueryRequest req, out DynamicParameters p)
    {
        p = new DynamicParameters();
        var conditions = new List<string>();
        if (!string.IsNullOrWhiteSpace(req.KeyName))
        {
            conditions.Add("key_name ILIKE @keyName");
            p.Add("keyName", $"%{req.KeyName}%");
        }
        if (!string.IsNullOrWhiteSpace(req.Tags))
        {
            conditions.Add("EXISTS (SELECT 1 FROM unnest(tags) t WHERE t ILIKE @tags)");
            p.Add("tags", $"%{req.Tags}%");
        }
        if (!string.IsNullOrWhiteSpace(req.CreateTimeStart))
        {
            conditions.Add("create_time >= @start");
            p.Add("start", DateTime.Parse(req.CreateTimeStart));
        }
        if (!string.IsNullOrWhiteSpace(req.CreateTimeEnd))
        {
            conditions.Add("create_time <= @end");
            p.Add("end", DateTime.Parse(req.CreateTimeEnd));
        }
        return conditions.Count > 0 ? "WHERE " + string.Join(" AND ", conditions) : string.Empty;
    }
}
