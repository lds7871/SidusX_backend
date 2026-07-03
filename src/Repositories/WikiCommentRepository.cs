using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class WikiCommentRepository
{
    private readonly NpgsqlDataSource _ds;
    public WikiCommentRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task<long> InsertAsync(WikiComment c)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.QuerySingleAsync<long>(
            "INSERT INTO wiki_comment (wiki_id, user_id, text, likes, create_time) VALUES (@wikiId, @userId, @text, 0, @createTime) RETURNING reply_id",
            new { wikiId = c.WikiId, userId = c.UserId, text = c.Text, createTime = c.CreateTime });
    }

    public async Task<bool> DeleteAsync(long replyId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        return await conn.ExecuteAsync("DELETE FROM wiki_comment WHERE reply_id = @replyId", new { replyId }) > 0;
    }

    public async Task IncrementLikesAsync(long replyId)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE wiki_comment SET likes = likes + 1 WHERE reply_id = @replyId", new { replyId });
    }

    public async Task<(List<WikiComment> Items, long Total)> PageByWikiIdAsync(long wikiId, int page, int pageSize)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var total = await conn.QuerySingleAsync<long>("SELECT COUNT(*) FROM wiki_comment WHERE wiki_id = @wikiId", new { wikiId });
        var offset = (page - 1) * pageSize;
        var items = (await conn.QueryAsync<WikiComment>(
            "SELECT * FROM wiki_comment WHERE wiki_id = @wikiId ORDER BY create_time DESC LIMIT @limit OFFSET @offset",
            new { wikiId, limit = pageSize, offset })).ToList();
        return (items, total);
    }
}
