package LDS.Person.service;

import LDS.Person.dto.request.WikiNewCreateRequest;
import LDS.Person.dto.request.WikiNewPageQueryRequest;
import LDS.Person.dto.request.WikiNewReviewRequest;
import LDS.Person.dto.response.WikiNewResponse;
import LDS.Person.dto.response.WikiNewListResponse;
import LDS.Person.dto.response.WikiNewReviewResponse;
import LDS.Person.dto.response.PageResponse;

/**
 * Wiki 新增业务逻辑接口
 */
public interface WikiNewService {

  /**
   * 新增 Wiki 记录
   * 
   * @param request 创建请求
   * @return Wiki 响应
   */
  WikiNewResponse createWikiNew(WikiNewCreateRequest request);

  /**
   * 分页查询 Wiki 新增列表
   * 支持按 wiki_states 和 wikinew_id 过滤
   * 
   * @param request 分页查询请求
   * @return 分页响应
   */
  PageResponse<WikiNewListResponse> pageQuery(WikiNewPageQueryRequest request);

  /**
   * 根据 Wiki ID 查询完整内容
   * 
   * @param wikinewId Wiki ID
   * @return Wiki 响应，如果不存在则返回 null
   */
  WikiNewResponse getWikiNewById(Long wikinewId);

  /**
   * 检查 Wiki 键名是否已存在
   * 
   * @param keyName Wiki 键名
   * @return true 表示键名已存在，false 表示不存在
   */
  boolean isKeyNameExists(String keyName);

  /**
   * 审核 Wiki 新增申请
   * 批准（wikiStates=1）时将内容复制到 wiki 表
   * 驳回（wikiStates=2）时仅更新状态
   * 
   * @param request 审核请求
   * @return 审核响应
   */
  WikiNewReviewResponse reviewWikiNew(WikiNewReviewRequest request);
}
