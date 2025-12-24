package LDS.Person.service;

import LDS.Person.entity.Wiki;
import LDS.Person.dto.request.WikiCreateRequest;
import LDS.Person.dto.request.WikiPageQueryRequest;
import LDS.Person.dto.response.WikiResponse;
import LDS.Person.dto.response.PageResponse;

/**
 * Wiki 业务逻辑接口
 */
public interface WikiService {
    
    /**
     * 新增 Wiki 记录
     * @param request 创建请求
     * @return Wiki 响应
     */
    WikiResponse createWiki(WikiCreateRequest request);
    
    /**
     * 删除 Wiki 记录
     * @param wikiId Wiki ID
     * @return true 删除成功，false 删除失败（记录不存在）
     */
    boolean deleteWiki(Long wikiId);
    
    /**
     * 分页查询 Wiki 列表
     * 支持多条件过滤和排序
     * 
     * @param request 分页查询请求
     * @return 分页响应
     */
    PageResponse<WikiResponse> pageQuery(WikiPageQueryRequest request);
}
