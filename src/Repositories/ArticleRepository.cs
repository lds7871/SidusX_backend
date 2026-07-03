using Dapper;
using Npgsql;
using SidusX.Models;
using SidusX.DTOs.Request;

namespace SidusX.Repositories;

public class ArticleRepository
{
    private readonly NpgsqlDataSource _ds;
    public ArticleRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(Article a)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<long>(
            @"INSERT INTO article (title, cover, info, texts, tags, create_time, update_time)
              VALUES (@title, @cover, @info, @texts, @tags, @createTime, @updateTime) RETURNING article_id",
            new { title = a.Title, cover = a.Cover, info = a.Info, texts = a.Texts, tags = a.Tags,
                  createTime = a.CreateTime, updateTime = a.UpdateTime });
    }

    public async Task<bool> DeleteAsync(long articleId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.ExecuteAsync("DELETE FROM article WHERE article_id = @articleId", new { articleId }) > 0;
    }

    public async Task<Article?> GetByIdAsync(long articleId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<Article>(
            "SELECT * FROM article WHERE article_id = @articleId", new { articleId });
    }

    public async Task UpdateAsync(Article a)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        await conn.ExecuteAsync(
            "UPDATE article SET title=@title, cover=@cover, info=@info, texts=@texts, tags=@tags, update_time=@updateTime WHERE article_id=@articleId",
            new { title = a.Title, cover = a.Cover, info = a.Info, texts = a.Texts, tags = a.Tags,
                  updateTime = DateTime.Now, articleId = a.ArticleId });
    }

    public async Task<(List<Article> Items, long Total)> PageQueryAsync(ArticleQueryRequest req)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var conds = new List<string>();
        var p = new DynamicParameters();
        if (!string.IsNullOrWhiteSpace(req.Title)) { conds.Add("title ILIKE @title"); p.Add("title", $"%{req.Title}%"); }
        if (!string.IsNullOrWhiteSpace(req.Tags)) { conds.Add("tags ILIKE @tags"); p.Add("tags", $"%{req.Tags}%"); }
        if (!string.IsNullOrWhiteSpace(req.CreateTimeStart)) { conds.Add("create_time >= @start"); p.Add("start", DateTime.Parse(req.CreateTimeStart)); }
        if (!string.IsNullOrWhiteSpace(req.CreateTimeEnd)) { conds.Add("create_time <= @end"); p.Add("end", DateTime.Parse(req.CreateTimeEnd)); }
        var where = conds.Count > 0 ? "WHERE " + string.Join(" AND ", conds) : "";
        var total = await conn.QuerySingleAsync<long>($"SELECT COUNT(*) FROM article {where}", p);
        var offset = (req.Page - 1) * req.PageSize;
        p.Add("limit", req.PageSize); p.Add("offset", offset);
        var items = (await conn.QueryAsync<Article>($"SELECT * FROM article {where} ORDER BY create_time DESC LIMIT @limit OFFSET @offset", p)).ToList();
        return (items, total);
    }

    public async Task<List<Article>> GetLatestAsync(int limit)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return (await conn.QueryAsync<Article>("SELECT * FROM article ORDER BY create_time DESC LIMIT @limit", new { limit })).ToList();
    }
}
