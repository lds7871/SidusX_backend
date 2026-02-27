package LDS.Person.repository;

import LDS.Person.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户 Mapper 接口
 */
@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (name, cover, phone, mail, password_hash, place, achievement_json, expired_time) " +
            "VALUES (#{name}, decode(#{cover}, 'base64'), #{phone}, #{mail}, #{passwordHash}, #{place}, " +
            "CAST(#{achievementJson} AS jsonb), #{expiredTime})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    int insert(User user);

    @Select("SELECT user_id, name, encode(cover, 'base64') AS cover, phone, mail, password_hash, place, " +
            "achievement_json::text AS achievement_json, expired_time " +
            "FROM users WHERE mail = #{mail}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "achievementJson", column = "achievement_json"),
            @Result(property = "expiredTime", column = "expired_time")
    })
    User selectByMail(String mail);

    @Select("SELECT user_id, name, encode(cover, 'base64') AS cover, phone, mail, password_hash, place, " +
            "achievement_json::text AS achievement_json, expired_time " +
            "FROM users WHERE phone = #{phone}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "achievementJson", column = "achievement_json"),
            @Result(property = "expiredTime", column = "expired_time")
    })
    User selectByPhone(String phone);

    @Select("SELECT user_id, name, encode(cover, 'base64') AS cover, phone, mail, password_hash, place, " +
            "achievement_json::text AS achievement_json, expired_time " +
            "FROM users WHERE user_id = #{userId}")
    @Results({
            @Result(property = "userId", column = "user_id"),
            @Result(property = "passwordHash", column = "password_hash"),
            @Result(property = "achievementJson", column = "achievement_json"),
            @Result(property = "expiredTime", column = "expired_time")
    })
    User selectById(Long userId);

    @Update("UPDATE users SET expired_time = #{expiredTime} WHERE user_id = #{userId}")
    int updateExpiredTime(@Param("userId") Long userId, @Param("expiredTime") java.time.LocalDateTime expiredTime);

    @Update("UPDATE users SET password_hash = #{passwordHash} WHERE user_id = #{userId}")
    int updatePasswordHash(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);

    @Update("UPDATE users SET cover = decode(#{cover}, 'base64') WHERE user_id = #{userId}")
    int updateCover(@Param("userId") Long userId, @Param("cover") String cover);
}
