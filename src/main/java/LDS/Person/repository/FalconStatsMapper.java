package LDS.Person.repository;

import LDS.Person.entity.FalconStats;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * SpaceX Falcon 统计数据表 Mapper 接口
 */
@Mapper
public interface FalconStatsMapper {

  @Insert("INSERT INTO falcon_stats (document_id, total_launches, total_landings, total_reflights, created_at) " +
      "VALUES (#{documentId}, #{totalLaunches}, #{totalLandings}, #{totalReflights}, #{createdAt})")
  @Options(useGeneratedKeys = true, keyProperty = "falconId", keyColumn = "falcon_id")
  int insert(FalconStats falconStats);

  @Select("SELECT falcon_id, document_id, total_launches, total_landings, total_reflights, created_at " +
      "FROM falcon_stats WHERE falcon_id = #{falconId}")
  @Results({
      @Result(property = "falconId", column = "falcon_id"),
      @Result(property = "documentId", column = "document_id"),
      @Result(property = "totalLaunches", column = "total_launches"),
      @Result(property = "totalLandings", column = "total_landings"),
      @Result(property = "totalReflights", column = "total_reflights"),
      @Result(property = "createdAt", column = "created_at")
  })
  FalconStats selectById(Long falconId);

  @Select("SELECT falcon_id, document_id, total_launches, total_landings, total_reflights, created_at " +
      "FROM falcon_stats ORDER BY created_at DESC")
  @Results({
      @Result(property = "falconId", column = "falcon_id"),
      @Result(property = "documentId", column = "document_id"),
      @Result(property = "totalLaunches", column = "total_launches"),
      @Result(property = "totalLandings", column = "total_landings"),
      @Result(property = "totalReflights", column = "total_reflights"),
      @Result(property = "createdAt", column = "created_at")
  })
  List<FalconStats> selectAll();

  @Delete("DELETE FROM falcon_stats WHERE falcon_id = #{falconId}")
  int deleteById(Long falconId);
}
