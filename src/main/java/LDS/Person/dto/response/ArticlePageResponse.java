package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章分页查询响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章分页查询结果")
public class ArticlePageResponse {

    @Schema(description = "总记录数", example = "100")
    private Long totalCount;

    @Schema(description = "总页数", example = "10")
    private Long totalPages;

    @Schema(description = "当前页码", example = "1")
    private Long currentPage;

    @Schema(description = "每页记录数", example = "10")
    private Long pageSize;

    @Schema(description = "文章列表")
    private List<ArticleResponse> records;
}
