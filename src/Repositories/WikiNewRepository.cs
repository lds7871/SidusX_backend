using Dapper;
using Npgsql;
using SidusX.Models;
using SidusX.DTOs.Request;

namespace SidusX.Repositories;

public class WikiNewRepository
{
    private readonly NpgsqlDataSource _ds;
    public WikiNewRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(WikiNew n)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = @"INSERT INTO wiki_new (key_name, texts, tags, version, create_time, create_user, update_time, update_user, wiki_states)
                    VALUES (@keyName, @texts, @tags, @version, @createTime, @createUser, @updateTime, @updateUser, @wikiStates)
                    RETURNING wikinew_id";
        return await conn.QuerySingleAsync<long>(sql, new
        {
            keyName = n.KeyName, texts = n.Texts, tags = n.Tags, version = n.Version,
            createTime = n.CreateTime, createUser = n.CreateUser,
            updateTime = n.UpdateTime, updateUser = n.UpdateUser, wikiStates = n.WikiStates
        });
    }

    public async Task<WikiNew?> GetByIdAsync(long id)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<WikiNew>("SELECT * FROM wiki_new WHERE wikinew_id = @id", new { id });
    }

    public async Task<bool> IsKeyNameExistsAsync(string keyName)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<int>("SELECT COUNT(*) FROM wiki_new WHERE key_name = @keyName", new { keyName }) > 0;
    }

    public async Task UpdateStateAsync(long id, int state)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE wiki_new SET wiki_states = @state WHERE wikinew_id = @id", new { id, state });
    }

    public async Task<(List<WikiNew> Items, long Total)> PageQueryAsync(WikiNewPageQueryRequest req)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var conds = new List<string>();
        var p = new DynamicParameters();
        if (!string.IsNullOrWhiteSpace(req.KeyName)) { conds.Add("key_name ILIKE @kn"); p.Add("kn", $"%{req.KeyName}%"); }
        if (req.WikiStates.HasValue) { conds.Add("wiki_states = @ws"); p.Add("ws", req.WikiStates); }
        var where = conds.Count > 0 ? "WHERE " + string.Join(" AND ", conds) : "";
        var total = await conn.QuerySingleAsync<long>($"SELECT COUNT(*) FROM wiki_new {where}", p);
        var offset = (req.Page - 1) * req.PageSize;
        p.Add("limit", req.PageSize); p.Add("offset", offset);
        var items = (await conn.QueryAsync<WikiNew>(
            $"SELECT * FROM wiki_new {where} ORDER BY create_time DESC LIMIT @limit OFFSET @offset", p)).ToList();
        return (items, total);
    }
}
