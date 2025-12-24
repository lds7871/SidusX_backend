package LDS.Person.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import LDS.Person.entity.Article;
import LDS.Person.dto.request.ArticleCreateRequest;
import LDS.Person.dto.request.ArticleQueryRequest;
import LDS.Person.dto.response.ArticleResponse;
import LDS.Person.dto.response.ArticleListResponse;
import LDS.Person.dto.response.ArticleResultResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.service.ArticleService;

/**
 * 文章控制器
 * 提供文章分页查询和新建功能
 */
@RestController
@RequestMapping("/api/article")
@Tag(name = "文章管理", description = "文章的查询和创建接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleService articleService;

    /**
     * 分页查询文章
     * 支持按标题和标签模糊查询
     */
    @PostMapping("/query")
    @Operation(summary = "分页查询文章列表", description = "支持按标题和标签进行模糊查询，返回简化的文章信息")
    public ResponseEntity<PageResponse<ArticleListResponse>> queryArticles(
            @RequestBody ArticleQueryRequest queryRequest) {
        try {
            logger.info("分页查询文章 - Page: {}, PageSize: {}, Title: {}, Tags: {}", 
                queryRequest.getPageNum(), queryRequest.getPageSize(), queryRequest.getTitle(), queryRequest.getTags());
            PageResponse<ArticleListResponse> response = articleService.pageQuery(queryRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("分页查询文章失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文章ID获取完整的文章内容
     */
    @GetMapping("/{articleId}")
    @Operation(summary = "获取完整文章内容", description = "根据文章ID获取文章的完整信息，包括所有字段")
    public ResponseEntity<ArticleResultResponse> getArticleById(
            @PathVariable Long articleId) {
        try {
            logger.info("根据ID查询文章 - ArticleId: {}", articleId);
            Article article = articleService.getById(articleId);
            
            if (article != null) {
                // 转换为响应DTO
                ArticleResponse response = new ArticleResponse();
                response.setArticleId(article.getArticleId());
                response.setTitle(article.getTitle());
                response.setCover(article.getCover());
                response.setInfo(article.getInfo());
                response.setTexts(article.getTexts());
                response.setTags(article.getTags());
                response.setCreateTime(article.getCreateTime());
                response.setUpdateTime(article.getUpdateTime());

                ArticleResultResponse resultResponse = ArticleResultResponse.builder()
                        .code(200)
                        .message("✅ 查询成功")
                        .data(response)
                        .timestamp(System.currentTimeMillis())
                        .build();

                return ResponseEntity.ok(resultResponse);
            } else {
                ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                        .code(404)
                        .message("文章不存在")
                        .timestamp(System.currentTimeMillis())
                        .build();
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("❌ 查询文章失败", e);
            ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                    .code(500)
                    .message("查询失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 新建文章
     */
    @PostMapping("/create")
    @Operation(summary = "新建文章", description = "创建一篇新的文章")
    public ResponseEntity<ArticleResultResponse> createArticle(
            @RequestBody ArticleCreateRequest createRequest) {
        try {
            logger.info("创建文章 - Title: {}", createRequest.getTitle());
            // 转换请求DTO为实体
            Article article = new Article();
            article.setTitle(createRequest.getTitle());
            article.setCover(createRequest.getCover());
            article.setInfo(createRequest.getInfo());
            article.setTexts(createRequest.getTexts());
            article.setTags(createRequest.getTags());

            // 创建文章
            boolean success = articleService.createArticle(article);

            if (success) {
                // 转换响应
                ArticleResponse response = new ArticleResponse();
                response.setArticleId(article.getArticleId());
                response.setTitle(article.getTitle());
                response.setCover(article.getCover());
                response.setInfo(article.getInfo());
                response.setTexts(article.getTexts());
                response.setTags(article.getTags());
                response.setCreateTime(article.getCreateTime());
                response.setUpdateTime(article.getUpdateTime());

                ArticleResultResponse resultResponse = ArticleResultResponse.builder()
                        .code(200)
                        .message("✅ 文章创建成功")
                        .data(response)
                        .timestamp(System.currentTimeMillis())
                        .build();

                return ResponseEntity.ok(resultResponse);
            } else {
                ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                        .code(500)
                        .message("创建失败: 未知错误")
                        .timestamp(System.currentTimeMillis())
                        .build();
                return ResponseEntity.status(500).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("❌ 文章创建失败", e);
            ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                    .code(500)
                    .message("创建失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
