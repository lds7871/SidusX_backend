package LDS.Person.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import LDS.Person.config.BypassIpWhitelist;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import LDS.Person.entity.Article;
import LDS.Person.dto.request.ArticleCreateRequest;
import LDS.Person.dto.request.ArticleQueryRequest;
import LDS.Person.dto.response.ArticleResponse;
import LDS.Person.dto.response.ArticleListResponse;
import LDS.Person.dto.response.ArticleResultResponse;
import LDS.Person.dto.response.ArticleLatestResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.service.ArticleService;

/**
 * 文章控制器
 * 提供文章分页查询和新建功能
 */
@RestController
@RequestMapping("/GHapi/article")
@Tag(name = "文章管理", description = "文章的查询和创建接口")
public class ArticleController {

    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 分页查询文章
     * 支持按标题和标签模糊查询
     */
    @PostMapping("/query")
    @BypassIpWhitelist(reason = "文章 分页查询接口")
    @Operation(summary = "分页查询文章列表", description = "支持按标题和标签进行模糊查询，返回简化的文章信息")
    public ResponseEntity<PageResponse<ArticleListResponse>> queryArticles(
            @RequestBody ArticleQueryRequest queryRequest) {
        try {
            log.info("分页查询文章 - Page: {}, PageSize: {}, Title: {}, Tags: {}",
                    queryRequest.getPageNum(), queryRequest.getPageSize(), queryRequest.getTitle(),
                    queryRequest.getTags());
            PageResponse<ArticleListResponse> response = articleService.pageQuery(queryRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("分页查询文章失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据文章ID获取完整的文章内容
     */
    @GetMapping("/{articleId}")
    @BypassIpWhitelist(reason = "文章 内容查询接口")
    @Operation(summary = "获取完整文章内容", description = "根据文章ID获取文章的完整信息，包括所有字段")
    public ResponseEntity<ArticleResultResponse> getArticleById(
            @PathVariable Long articleId) {
        try {
            log.info("根据ID查询文章 - ArticleId: {}", articleId);
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
            log.error("❌ 查询文章失败", e);
            ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                    .code(500)
                    .message("查询失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 获取最新一篇文章的摘要信息
     */
    @GetMapping("/latest")
    @BypassIpWhitelist(reason = "文章 最新文章接口")
    @Operation(summary = "获取最新文章", description = "返回最新文章的ID、标题、简介与标签")
    public ResponseEntity<ArticleResultResponse> getLatestArticle() {
        try {
            log.info("查询最新文章");
            Article article = articleService.getLatestArticle();

            if (article != null) {
                ArticleLatestResponse latestResponse = ArticleLatestResponse.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .info(article.getInfo())
                        .tags(article.getTags())
                        .build();

                ArticleResultResponse resultResponse = ArticleResultResponse.builder()
                        .code(200)
                        .message("✅ 最新文章获取成功")
                        .data(latestResponse)
                        .timestamp(System.currentTimeMillis())
                        .build();
                return ResponseEntity.ok(resultResponse);
            } else {
                ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                        .code(404)
                        .message("暂无文章")
                        .timestamp(System.currentTimeMillis())
                        .build();
                return ResponseEntity.status(404).body(errorResponse);
            }
        } catch (Exception e) {
            log.error("❌ 获取最新文章失败", e);
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
            log.info("创建文章 - Title: {}", createRequest.getTitle());
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
            log.error("❌ 文章创建失败", e);
            ArticleResultResponse errorResponse = ArticleResultResponse.builder()
                    .code(500)
                    .message("创建失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
