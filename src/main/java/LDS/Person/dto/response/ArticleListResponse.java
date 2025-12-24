package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章列表查询响应DTO - 仅返回简化信息
 */
@Data
@NoArgsConstructor
@Schema(description = "文章列表信息")
public class ArticleListResponse {

    @Schema(description = "文章ID", example = "1")
    private Long articleId;

    @Schema(description = "封面图片地址", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "文章简介", example = "这是一篇技术分享文章")
    private String info;

    @Schema(description = "文章标签（逗号分隔）", example = "Java,Spring,技术")
    private String tags;
}
