package LDS.Person.service;

import LDS.Person.entity.Article;
import LDS.Person.dto.request.ArticleQueryRequest;
import LDS.Person.dto.response.ArticleResponse;
import LDS.Person.dto.response.PageResponse;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {

    /**
     * 分页查询文章，支持按标题和标签模糊查询
     *
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param title 标题（模糊查询）
     * @param tags 标签（模糊查询）
     * @return 分页结果
     */
    PageResponse<ArticleResponse> queryArticleByPage(Integer pageNum, Integer pageSize, String title, String tags);

    /**
     * 分页查询文章（手动分页方式）
     *
     * @param request 查询请求
     * @return 分页响应
     */
    PageResponse<ArticleResponse> pageQuery(ArticleQueryRequest request);

    /**
     * 新建文章
     *
     * @param article 文章信息
     * @return 是否创建成功
     */
    boolean createArticle(Article article);

    /**
     * 根据ID查询文章
     *
     * @param articleId 文章ID
     * @return 文章信息
     */
    Article getById(Long articleId);

    /**
     * 根据ID更新文章
     *
     * @param article 文章信息
     * @return 是否更新成功
     */
    boolean updateById(Article article);

    /**
     * 根据ID删除文章
     *
     * @param articleId 文章ID
     * @return 是否删除成功
     */
    boolean removeById(Long articleId);

    /**
     * 查询所有文章
     *
     * @return 文章列表
     */
    List<Article> list();
}
