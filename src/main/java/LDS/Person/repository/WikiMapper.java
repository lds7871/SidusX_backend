package LDS.Person.repository;

import LDS.Person.entity.Wiki;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * Wiki 数据访问层 (MyBatis Mapper)
 * 只负责基本的 insert 和 delete 操作
 * 所有 SELECT 查询操作在 WikiServiceImpl 中通过 JdbcTemplate 实现
 */
@Mapper
public interface WikiMapper {
    
    /**
     * 新增 Wiki 记录
     * @param wiki Wiki 实体
     * @return 插入的记录数
     */
    @Insert("INSERT INTO wiki (key_name, texts, tags, version, create_time, create_user, update_time, update_user) " +
            "VALUES (#{keyName}, #{texts}, #{tags, typeHandler=org.apache.ibatis.type.ArrayTypeHandler}, " +
            "#{version}, #{createTime}, #{createUser}, #{updateTime}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "wikiId", keyColumn = "wiki_id")
    int insertWiki(Wiki wiki);
    
    /**
     * 根据 wiki_id 删除 Wiki 记录
     * @param wikiId Wiki ID
     * @return 删除的记录数
     */
    @Delete("DELETE FROM wiki WHERE wiki_id = #{wikiId}")
    int deleteWikiById(Long wikiId);
}
