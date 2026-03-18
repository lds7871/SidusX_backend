package LDS.Person.repository;

import LDS.Person.entity.MsShip;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * MS_SHIP表数据访问层 (MyBatis Mapper)
 * 用于处理 ms_ship 表的数据操作
 */
@Mapper
public interface MsShipMapper {

  /**
   * 新增 MS_SHIP 记录
   *
   * @param msShip MsShip 实体
   * @return 插入的记录数
   */
  @Insert("INSERT INTO ms_ship (content) VALUES (#{content}::jsonb)")
  @Options(useGeneratedKeys = true, keyProperty = "msId", keyColumn = "ms_id")
  int insertMsShip(MsShip msShip);

  /**
   * 根据 MS_ID 查询单条记录
   *
   * @param msId MS_SHIP ID
   * @return MsShip 实体
   */
  @Select("SELECT ms_id, content FROM ms_ship WHERE ms_id = #{msId}")
  MsShip selectMsShipById(Long msId);
}
