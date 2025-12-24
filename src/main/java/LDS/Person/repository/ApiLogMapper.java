package LDS.Person.repository;

import LDS.Person.entity.ApiLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 访问日志 Mapper 接口
 */
@Mapper
public interface ApiLogMapper {

    @Insert("INSERT INTO api_log (ip, api, states, create_time) " +
            "VALUES (#{ip}, #{api}, #{states}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(ApiLog apiLog);

    @Select("SELECT id, ip, api, states, create_time FROM api_log WHERE id = #{id}")
    @Results({
        @Result(property = "createTime", column = "create_time")
    })
    ApiLog selectById(Integer id);

    @Select("SELECT id, ip, api, states, create_time FROM api_log")
    @Results({
        @Result(property = "createTime", column = "create_time")
    })
    List<ApiLog> selectAll();

    @Delete("DELETE FROM api_log WHERE id = #{id}")
    int deleteById(Integer id);
}
