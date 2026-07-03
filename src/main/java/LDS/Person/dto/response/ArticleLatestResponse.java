package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最新文章摘要
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "最新文章摘要")
public class ArticleLatestResponse {

  @Schema(description = "文章ID", example = "1")
  private Long articleId;

  @Schema(description = "文章标题", example = "技术分享")
  private String title;

  @Schema(description = "文章简介", example = "这是一篇技术分享文章")
  private String info;

  @Schema(description = "文章标签（逗号分隔）", example = "Java,Spring,技术")
  private String tags;
}
