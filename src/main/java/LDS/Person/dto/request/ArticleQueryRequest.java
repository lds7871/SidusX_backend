package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章分页查询请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "文章分页查询请求")
public class ArticleQueryRequest {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "文章标题（模糊查询），可选", example = "技术")
    private String title;

    @Schema(description = "文章标签（模糊查询），可选", example = "Java")
    private String tags;
}
