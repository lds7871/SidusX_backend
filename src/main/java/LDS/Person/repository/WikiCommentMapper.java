package LDS.Person.repository;

import LDS.Person.entity.WikiComment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * Wiki留言数据访问层 (MyBatis Mapper)
 * 只负责基本的 insert 和 delete 操作
 * 所有 SELECT 查询操作在 WikiCommentServiceImpl 中通过 JdbcTemplate 实现
 */
@Mapper
public interface WikiCommentMapper {

  /**
   * 新增Wiki留言记录
   * 
   * @param wikiComment Wiki留言实体
   * @return 插入的记录数
   */
  @Insert("INSERT INTO wiki_comment (wiki_id, user_id, text, likes, create_time) " +
      "VALUES (#{wikiId}, #{userId}, #{text}, #{likes}, #{createTime})")
  @Options(useGeneratedKeys = true, keyProperty = "replyId", keyColumn = "reply_id")
  int insertWikiComment(WikiComment wikiComment);

  /**
   * 根据 reply_id 删除Wiki留言记录
   * 
   * @param replyId 留言ID
   * @return 删除的记录数
   */
  @Delete("DELETE FROM wiki_comment WHERE reply_id = #{replyId}")
  int deleteWikiCommentById(Long replyId);
}
