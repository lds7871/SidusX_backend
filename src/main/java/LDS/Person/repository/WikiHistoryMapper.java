package LDS.Person.repository;

import LDS.Person.entity.WikiHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * Wiki 历史记录数据访问层 (MyBatis Mapper)
 * 用于处理wiki_history表的数据操作
 */
@Mapper
public interface WikiHistoryMapper {

  /**
   * 新增 Wiki 历史记录
   * 
   * @param wikiHistory WikiHistory 实体
   * @return 插入的记录数
   */
  @Insert("INSERT INTO wiki_history (wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user, backup_time) "
      +
      "VALUES (#{wikiId}, #{keyName}, #{texts}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler}, " +
      "#{version}, #{createTime}, #{createUser}, #{updateTime}, #{updateUser}, #{backupTime})")
  @Options(useGeneratedKeys = true, keyProperty = "historyId", keyColumn = "history_id")
  int insertWikiHistory(WikiHistory wikiHistory);
}
