using Dapper;
using Npgsql;
using NpgsqlTypes;
using SidusX.Models;

namespace SidusX.Repositories;

public class UserRepository
{
    private readonly NpgsqlDataSource _dataSource;

    public UserRepository(NpgsqlDataSource dataSource) => _dataSource = dataSource;

    public async Task<long> InsertAsync(User user)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        // Convert base64 string cover to bytea
        var sql = user.Cover != null
            ? @"INSERT INTO users (name, cover, phone, mail, password_hash, place, achievement_json, expired_time)
                VALUES (@name, decode(@cover, 'base64'), @phone, @mail, @passwordHash, @place, @achievementJson::jsonb, @expiredTime)
                RETURNING user_id"
            : @"INSERT INTO users (name, phone, mail, password_hash, place, achievement_json, expired_time)
                VALUES (@name, @phone, @mail, @passwordHash, @place, @achievementJson::jsonb, @expiredTime)
                RETURNING user_id";
        return await conn.QuerySingleAsync<long>(sql, new
        {
            name = user.Name, cover = user.Cover, phone = user.Phone, mail = user.Mail,
            passwordHash = user.PasswordHash, place = user.Place,
            achievementJson = user.AchievementJson ?? "{}", expiredTime = user.ExpiredTime
        });
    }

    public async Task<User?> SelectByMailAsync(string mail)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = @"SELECT user_id, name, encode(cover,'base64') AS cover, phone, mail, password_hash,
                           place, achievement_json::text AS achievement_json, expired_time
                    FROM users WHERE mail = @mail";
        return await conn.QueryFirstOrDefaultAsync<User>(sql, new { mail }, commandType: null);
    }

    public async Task<User?> SelectByPhoneAsync(string phone)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = @"SELECT user_id, name, encode(cover,'base64') AS cover, phone, mail, password_hash,
                           place, achievement_json::text AS achievement_json, expired_time
                    FROM users WHERE phone = @phone";
        return await conn.QueryFirstOrDefaultAsync<User>(sql, new { phone });
    }

    public async Task<User?> SelectByIdAsync(long userId)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = @"SELECT user_id, name, encode(cover,'base64') AS cover, phone, mail, password_hash,
                           place, achievement_json::text AS achievement_json, expired_time
                    FROM users WHERE user_id = @userId";
        return await conn.QueryFirstOrDefaultAsync<User>(sql, new { userId });
    }

    public async Task UpdateExpiredTimeAsync(long userId, DateTime expiredTime)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE users SET expired_time = @expiredTime WHERE user_id = @userId",
            new { userId, expiredTime });
    }

    public async Task UpdatePasswordHashAsync(long userId, string passwordHash)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE users SET password_hash = @passwordHash WHERE user_id = @userId",
            new { userId, passwordHash });
    }

    public async Task UpdateCoverAsync(long userId, string cover)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE users SET cover = decode(@cover, 'base64') WHERE user_id = @userId",
            new { userId, cover });
    }

    public async Task UpdatePlaceAsync(long userId, string place)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE users SET place = @place WHERE user_id = @userId", new { userId, place });
    }

    public async Task UpdateAchievementJsonAsync(long userId, string achievementJson)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        await conn.ExecuteAsync("UPDATE users SET achievement_json = @achievementJson::jsonb WHERE user_id = @userId",
            new { userId, achievementJson });
    }

    public async Task<string?> GetNameByIdAsync(long userId)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<string>("SELECT name FROM users WHERE user_id = @userId", new { userId });
    }
}
