package LDS.Person.service;

import LDS.Person.dto.request.WikiCommentByWikiIdRequest;
import LDS.Person.dto.request.WikiCommentCreateRequest;
import LDS.Person.dto.response.WikiCommentResponse;

import java.util.List;

/**
 * Wiki留言业务逻辑接口
 */
public interface WikiCommentService {

  /**
   * 根据wiki_id查询所有留言
   * 返回的留言包含对应用户的name和cover字段
   *
   * @param wikiId Wiki ID
   * @return Wiki留言响应列表
   */
  List<WikiCommentResponse> getCommentsByWikiId(Long wikiId);

  /**
   * 根据wiki_id查询所有留言（通过Request对象）
   *
   * @param request 查询请求
   * @return Wiki留言响应列表
   */
  List<WikiCommentResponse> getCommentsByWikiId(WikiCommentByWikiIdRequest request);

  /**
   * 添加Wiki留言
   *
   * @param request 留言请求（只包含wiki_id、user_id、text）
   * @return true 添加成功，false 添加失败
   */
  boolean addComment(WikiCommentCreateRequest request);

  /**
   * 删除Wiki留言记录
   *
   * @param replyId 留言ID
   * @return true 删除成功，false 删除失败（记录不存在）
   */
  boolean deleteComment(Long replyId);

  /**
   * 点赞留言
   *
   * @param replyId 留言ID
   * @return true 点赞成功，false 点赞失败
   */
  boolean likeComment(Long replyId);
}
