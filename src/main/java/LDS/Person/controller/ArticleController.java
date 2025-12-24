package LDS.Person.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import LDS.Person.entity.Article;
import LDS.Person.dto.request.ArticleCreateRequest;
import LDS.Person.dto.request.ArticleQueryRequest;
import LDS.Person.dto.response.ArticlePageResponse;
import LDS.Person.dto.response.ArticleResponse;
import LDS.Person.dto.response.ArticleResultResponse;
import LDS.Person.service.ArticleService;

import java.util.List;
import java.util.stream.Collectors;

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
    @Operation(summary = "分页查询文章", description = "支持按标题和标签进行模糊查询")
    public ResponseEntity<ArticleResultResponse> queryArticles(
            @RequestBody ArticleQueryRequest queryRequest) {
        try {
            // 执行分页查询
            Page<Article> page = articleService.queryArticleByPage(
                    queryRequest.getPageNum(),
                    queryRequest.getPageSize(),
                    queryRequest.getTitle(),
                    queryRequest.getTags()
            );

            // 转换为响应DTO
            List<ArticleResponse> articles = page.getRecords().stream()
                    .map(article -> {
                        ArticleResponse response = new ArticleResponse();
                        response.setArticleId(article.getArticleId());
                        response.setTitle(article.getTitle());
                        response.setCover(article.getCover());
                        response.setInfo(article.getInfo());
                        response.setTexts(article.getTexts());
                        response.setTags(article.getTags());
                        response.setCreateTime(article.getCreateTime());
                        response.setUpdateTime(article.getUpdateTime());
                        return response;
                    })
                    .collect(Collectors.toList());

            // 构建分页响应
            ArticlePageResponse pageResponse = new ArticlePageResponse(
                    page.getTotal(),
                    page.getPages(),
                    page.getCurrent(),
                    page.getSize(),
                    articles
            );

            ArticleResultResponse resultResponse = ArticleResultResponse.builder()
                    .code(200)
                    .message("✅ 查询成功")
                    .data(pageResponse)
                    .timestamp(System.currentTimeMillis())
                    .build();

            return ResponseEntity.ok(resultResponse);
        } catch (Exception e) {
            logger.error("❌ 文章查询失败", e);
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
