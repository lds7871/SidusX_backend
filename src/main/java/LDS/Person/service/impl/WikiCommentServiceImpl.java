package LDS.Person.service.impl;

import LDS.Person.dto.request.WikiCommentByWikiIdRequest;
import LDS.Person.dto.request.WikiCommentCreateRequest;
import LDS.Person.dto.response.WikiCommentResponse;
import LDS.Person.entity.WikiComment;
import LDS.Person.repository.WikiCommentMapper;
import LDS.Person.service.WikiCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Wiki留言业务逻辑实现
 * 使用JdbcTemplate处理查询操作，使用MyBatis处理新增和删除操作
 */
@Service
public class WikiCommentServiceImpl implements WikiCommentService {

  private static final Logger log = LoggerFactory.getLogger(WikiCommentServiceImpl.class);

  private final WikiCommentMapper wikiCommentMapper;
  private final JdbcTemplate jdbcTemplate;

  public WikiCommentServiceImpl(WikiCommentMapper wikiCommentMapper, JdbcTemplate jdbcTemplate) {
    this.wikiCommentMapper = wikiCommentMapper;
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * 根据wiki_id查询所有留言
   * 联合查询wiki_comment和users表，获取用户的name和cover字段
   */
  @Override
  public List<WikiCommentResponse> getCommentsByWikiId(Long wikiId) {
    String sql = "SELECT " +
        "wc.reply_id, " +
        "wc.wiki_id, " +
        "wc.user_id, " +
        "wc.text, " +
        "wc.likes, " +
        "TO_CHAR(wc.create_time, 'YYYY-MM-DD HH24:MI:SS') as create_time, " +
        "u.name as user_name, " +
        "encode(u.cover, 'base64') as user_cover " +
        "FROM wiki_comment wc " +
        "LEFT JOIN users u ON wc.user_id = u.user_id " +
        "WHERE wc.wiki_id = ? " +
        "ORDER BY wc.create_time DESC";

    return jdbcTemplate.query(sql, new Object[] { wikiId }, (rs, rowNum) -> {
      WikiCommentResponse response = new WikiCommentResponse();
      response.setReplyId(rs.getLong("reply_id"));
      response.setWikiId(rs.getLong("wiki_id"));
      response.setUserId(rs.getLong("user_id"));
      response.setText(rs.getString("text"));
      response.setLikes(rs.getInt("likes"));
      response.setCreateTime(rs.getString("create_time"));
      response.setUserName(rs.getString("user_name"));
      response.setUserCover(rs.getString("user_cover"));
      return response;
    });
  }

  /**
   * 根据wiki_id查询所有留言（通过Request对象）
   */
  @Override
  public List<WikiCommentResponse> getCommentsByWikiId(WikiCommentByWikiIdRequest request) {
    if (request == null || request.getWikiId() == null) {
      log.warn("Wiki ID is null, returning empty list");
      return List.of();
    }
    return getCommentsByWikiId(request.getWikiId());
  }

  /**
   * 添加Wiki留言
   */
  @Override
  public boolean addComment(WikiCommentCreateRequest request) {
    if (request == null || request.getWikiId() == null || request.getUserId() == null
        || request.getText() == null || request.getText().trim().isEmpty()) {
      throw new IllegalArgumentException("wiki_id、user_id、text 不能为空");
    }

    WikiComment wikiComment = new WikiComment();
    wikiComment.setWikiId(request.getWikiId());
    wikiComment.setUserId(request.getUserId());
    wikiComment.setText(request.getText().trim());

    try {
      int result = wikiCommentMapper.insertWikiComment(wikiComment);
      return result > 0;
    } catch (Exception e) {
      log.error("Failed to add wiki comment. wikiId: {}, userId: {}",
          request.getWikiId(), request.getUserId(), e);
      return false;
    }
  }

  /**
   * 删除Wiki留言记录
   */
  @Override
  public boolean deleteComment(Long replyId) {
    if (replyId == null) {
      log.warn("Reply ID is null, cannot delete");
      return false;
    }
    try {
      int result = wikiCommentMapper.deleteWikiCommentById(replyId);
      return result > 0;
    } catch (Exception e) {
      log.error("Failed to delete wiki comment with id: {}", replyId, e);
      return false;
    }
  }

  /**
   * 点赞留言
   */
  @Override
  public boolean likeComment(Long replyId) {
    if (replyId == null) {
      log.warn("Reply ID is null, cannot like");
      return false;
    }
    try {
      String sql = "UPDATE wiki_comment SET likes = likes + 1 WHERE reply_id = ?";
      int result = jdbcTemplate.update(sql, replyId);
      return result > 0;
    } catch (Exception e) {
      log.error("Failed to like wiki comment with id: {}", replyId, e);
      return false;
    }
  }
}
