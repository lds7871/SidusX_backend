package LDS.Person.repository;

import LDS.Person.entity.RecentLaunch;
import org.apache.ibatis.annotations.*;

/**
 * 最近发射数据表 Mapper 接口
 */
@Mapper
public interface RecentLaunchMapper {

  /**
   * 插入一条发射记录，data 字段转换为 JSONB
   */
  @Insert("INSERT INTO recent_launch (data, get_time) VALUES (CAST(#{data} AS jsonb), #{getTime})")
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  int insert(RecentLaunch recentLaunch);

  /**
   * 查询表中记录总数
   */
  @Select("SELECT COUNT(*) FROM recent_launch")
  int selectCount();

  /**
   * 删除多余记录，只保留最新的 5 条（按 get_time 降序）
   */
  @Delete("DELETE FROM recent_launch WHERE id NOT IN (" +
      "SELECT id FROM recent_launch ORDER BY get_time DESC LIMIT 5" +
      ")")
  int deleteExceeding();

  /**
   * 查询最新一条发射记录
   */
  @Select("SELECT id, data::text AS data, get_time FROM recent_launch ORDER BY get_time DESC LIMIT 1")
  @Results({
      @Result(property = "id", column = "id"),
      @Result(property = "data", column = "data"),
      @Result(property = "getTime", column = "get_time")
  })
  RecentLaunch selectLatest();
}
