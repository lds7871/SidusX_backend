using Dapper;
using Npgsql;
using SidusX.Models;
using SidusX.DTOs.Request;

namespace SidusX.Repositories;

public class WikiReviewRepository
{
    private readonly NpgsqlDataSource _ds;
    public WikiReviewRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(WikiReview r)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = @"INSERT INTO wiki_review (wiki_id, texts, tags, version, update_time, update_user, wiki_states)
                    VALUES (@wikiId, @texts, @tags, @version, @updateTime, @updateUser, @wikiStates) RETURNING wikireview_id";
        return await conn.QuerySingleAsync<long>(sql, new
        {
            wikiId = r.WikiId, texts = r.Texts, tags = r.Tags, version = r.Version,
            updateTime = r.UpdateTime, updateUser = r.UpdateUser, wikiStates = r.WikiStates
        });
    }

    public async Task<WikiReview?> GetByIdAsync(long id)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<WikiReview>(
            "SELECT * FROM wiki_review WHERE wikireview_id = @id", new { id });
    }

    public async Task UpdateStateAsync(long id, int state)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE wiki_review SET wiki_states = @state WHERE wikireview_id = @id", new { id, state });
    }

    public async Task<(List<WikiReview> Items, long Total)> PageQueryAsync(WikiReviewPageQueryRequest req)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var conds = new List<string>();
        var p = new DynamicParameters();
        if (req.WikiId.HasValue) { conds.Add("wiki_id = @wikiId"); p.Add("wikiId", req.WikiId); }
        if (req.WikiStates.HasValue) { conds.Add("wiki_states = @wikiStates"); p.Add("wikiStates", req.WikiStates); }
        var where = conds.Count > 0 ? "WHERE " + string.Join(" AND ", conds) : "";
        var total = await conn.QuerySingleAsync<long>($"SELECT COUNT(*) FROM wiki_review {where}", p);
        var offset = (req.Page - 1) * req.PageSize;
        p.Add("limit", req.PageSize); p.Add("offset", offset);
        var items = (await conn.QueryAsync<WikiReview>(
            $"SELECT * FROM wiki_review {where} ORDER BY update_time DESC LIMIT @limit OFFSET @offset", p)).ToList();
        return (items, total);
    }
}
