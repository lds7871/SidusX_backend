using Dapper;
using Npgsql;
using SidusX.Models;

namespace SidusX.Repositories;

public class WikiHistoryRepository
{
    private readonly NpgsqlDataSource _ds;
    public WikiHistoryRepository(NpgsqlDataSource ds) => _ds = ds;

    public async Task InsertAsync(WikiHistory h)
    {
        await using var conn = await _ds.OpenConnectionAsync();
        var sql = @"INSERT INTO wiki_history (wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user, backup_time)
                    VALUES (@wikiId, @keyName, @texts, @tags, @version, @createTime, @createUser, @updateTime, @updateUser, @backupTime)";
        await conn.ExecuteAsync(sql, new
        {
            wikiId = h.WikiId, keyName = h.KeyName, texts = h.Texts, tags = h.Tags,
            version = h.Version, createTime = h.CreateTime, createUser = h.CreateUser,
            updateTime = h.UpdateTime, updateUser = h.UpdateUser, backupTime = h.BackupTime
        });
    }
}
