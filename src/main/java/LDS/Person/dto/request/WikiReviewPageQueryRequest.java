package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki 审核分页查询请求 DTO
 */
@Getter
@Setter
@Schema(description = "Wiki 审核分页查询请求")
public class WikiReviewPageQueryRequest {

    @JsonProperty("page")
    @Schema(description = "当前页码", defaultValue = "1")
    private Integer page = 1;

    @JsonProperty("page_size")
    @Schema(description = "每页数量", defaultValue = "10")
    private Integer pageSize = 10;

    @JsonProperty("wiki_id")
    @Schema(description = "关联的 Wiki ID（精确匹配）")
    private Long wikiId;

    @JsonProperty("wiki_states")
    @Schema(description = "审核状态：0待审核，1通过，2拒绝（精确匹配）")
    private Integer wikiStates;

    @JsonProperty("update_user")
    @Schema(description = "更新用户 ID（模糊匹配）")
    private String updateUser;
}
