package LDS.Person.repository;

import LDS.Person.entity.WikiNew;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

/**
 * Wiki 新增数据访问层 (MyBatis Mapper)
 * 用于处理 wiki_new 表的数据操作
 */
@Mapper
public interface WikiNewMapper {

  /**
   * 新增 Wiki 记录
   * 
   * @param wikiNew WikiNew 实体
   * @return 插入的记录数
   */
  @Insert("INSERT INTO wiki_new (key_name, texts, tags, version, create_time, create_user, update_time, update_user, wiki_states) "
      +
      "VALUES (#{keyName}, #{texts}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler}, " +
      "#{version}, #{createTime}, #{createUser}, #{updateTime}, #{updateUser}, #{wikiStates})")
  @Options(useGeneratedKeys = true, keyProperty = "wikinewId", keyColumn = "wikinew_id")
  int insertWikiNew(WikiNew wikiNew);

  /**
   * 根据 Wiki ID 查询单条记录
   * 
   * @param wikinewId Wiki ID
   * @return WikiNew 实体
   */
  @Select("SELECT wikinew_id, key_name, texts, tags, version, " +
      "create_time, create_user, update_time, update_user, wiki_states " +
      "FROM wiki_new WHERE wikinew_id = #{wikinewId}")
  WikiNew selectWikiNewById(Long wikinewId);

  /**
   * 根据键名查询单条记录
   * 
   * @param keyName Wiki 键名
   * @return WikiNew 实体
   */
  @Select("SELECT wikinew_id, key_name, texts, tags, version, " +
      "create_time, create_user, update_time, update_user, wiki_states " +
      "FROM wiki_new WHERE key_name = #{keyName}")
  WikiNew selectByKeyName(String keyName);

  /**
   * 删除 Wiki 记录（通过 ID）
   * 
   * @param wikinewId Wiki ID
   * @return 删除的记录数
   */
  @Delete("DELETE FROM wiki_new WHERE wikinew_id = #{wikinewId}")
  int deleteWikiNewById(Long wikinewId);
}
