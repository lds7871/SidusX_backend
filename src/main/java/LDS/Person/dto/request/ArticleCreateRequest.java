package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新建文章请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "新建文章请求")
public class ArticleCreateRequest {

    @Schema(description = "文章标题", example = "技术分享")
    private String title;

    @Schema(description = "封面图片地址", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "文章简介", example = "这是一篇技术分享文章")
    private String info;

    @Schema(description = "文章正文内容", example = "文章内容...")
    private String texts;

    @Schema(description = "文章标签（逗号分隔）", example = "Java,Spring,技术")
    private String tags;
}
