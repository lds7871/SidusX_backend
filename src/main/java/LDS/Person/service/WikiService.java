package LDS.Person.service;

import LDS.Person.dto.request.WikiCreateRequest;
import LDS.Person.dto.request.WikiPageQueryRequest;
import LDS.Person.dto.response.LatestWikiSummaryResponse;
import LDS.Person.dto.response.WikiResponse;
import LDS.Person.dto.response.PageResponse;

/**
 * Wiki 业务逻辑接口
 */
public interface WikiService {

    /**
     * 新增 Wiki 记录
     * 
     * @param request 创建请求
     * @return Wiki 响应
     */
    WikiResponse createWiki(WikiCreateRequest request);

    /**
     * 删除 Wiki 记录
     * 
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

    /**
     * 检查 Wiki 键名是否已存在
     * 
     * @param keyName Wiki 键名
     * @return true 表示键名已存在，false 表示不存在
     */
    boolean isKeyNameExists(String keyName);

    /**
     * 根据 Wiki ID 查询完整内容
     * 
     * @param wikiId Wiki ID
     * @return Wiki 响应，如果不存在则返回 null
     */
    WikiResponse getWikiById(Long wikiId);

    /**
     * 更新 Wiki 记录
     * 
     * @param wikiId  Wiki ID
     * @param request 更新请求
     * @return 更新后的 Wiki 响应
     */
    WikiResponse updateWiki(Long wikiId, LDS.Person.dto.request.WikiUpdateRequest request);

    /**
     * 随机获取一个 Wiki 的完整内容
     * 
     * @return 随机选中的 Wiki 响应，如果不存在则返回 null
     */
    WikiResponse getRandomWiki();

    /**
     * 随机获取四个 Wiki 的列表（分页格式）
     * 
     * @return 包含四个随机 Wiki 的分页响应
     */
    PageResponse<WikiResponse> getRandomWikis();

    /**
     * 获取最新更新过的 Wiki 元信息
     * 
     * @return 最新更新 Wiki 的摘要数据
     */
    LatestWikiSummaryResponse getLatestUpdatedWiki();
}
