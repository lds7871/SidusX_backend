package LDS.Person.repository;

import LDS.Person.entity.Wiki;
import LDS.Person.dto.request.WikiPageQueryRequest;
import org.apache.ibatis.annotations.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Wiki 数据访问层 (MyBatis Mapper)
 * 负责与数据库的交互
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
    
    /**
     * 根据 key_name 查询 Wiki（检查唯一性）
     * @param keyName 键名
     * @return Wiki 实体或 null
     */
    @Select("SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user " +
            "FROM wiki WHERE key_name = #{keyName}")
    Wiki selectByKeyName(String keyName);
    
    /**
     * 分页查询 Wiki 列表
     * 支持多条件过滤：key_name 模糊匹配、tags 模糊匹配、create_time 范围查询
     * 
     * @param request 查询请求（包含分页和过滤条件）
     * @return Wiki 列表
     */
    @Select({
        "<script>",
        "SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user ",
        "FROM wiki WHERE 1=1",
        "<if test='keyName != null and keyName != \"\"'>",
        "  AND key_name ILIKE CONCAT('%', #{keyName}, '%')",
        "</if>",
        "<if test='tags != null and tags != \"\"'>",
        "  AND tags::text ILIKE CONCAT('%', #{tags}, '%')",
        "</if>",
        "<if test='createTimeStart != null and createTimeStart != \"\"'>",
        "  AND create_time >= #{createTimeStart}",
        "</if>",
        "<if test='createTimeEnd != null and createTimeEnd != \"\"'>",
        "  AND create_time <= #{createTimeEnd}",
        "</if>",
        "ORDER BY create_time DESC",
        "LIMIT #{pageSize} OFFSET #{offset}",
        "</script>"
    })
    List<Wiki> selectPageList(WikiPageQueryRequest request, int offset);
    
    /**
     * 查询符合条件的总记录数
     * 用于计算分页总数
     * 
     * @param request 查询请求（包含过滤条件）
     * @return 总记录数
     */
    @Select({
        "<script>",
        "SELECT COUNT(*) FROM wiki WHERE 1=1",
        "<if test='keyName != null and keyName != \"\"'>",
        "  AND key_name ILIKE CONCAT('%', #{keyName}, '%')",
        "</if>",
        "<if test='tags != null and tags != \"\"'>",
        "  AND tags::text ILIKE CONCAT('%', #{tags}, '%')",
        "</if>",
        "<if test='createTimeStart != null and createTimeStart != \"\"'>",
        "  AND create_time >= #{createTimeStart}",
        "</if>",
        "<if test='createTimeEnd != null and createTimeEnd != \"\"'>",
        "  AND create_time <= #{createTimeEnd}",
        "</if>",
        "</script>"
    })
    long countByCondition(WikiPageQueryRequest request);
    
    /**
     * 根据 wiki_id 查询单条记录
     * @param wikiId Wiki ID
     * @return Wiki 实体或 null
     */
    @Select("SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user " +
            "FROM wiki WHERE wiki_id = #{wikiId}")
    Wiki selectById(Long wikiId);
}
