package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新 Wiki 审核状态请求 DTO
 */
@Getter
@Setter
@Schema(description = "更新 Wiki 审核状态请求")
public class WikiReviewUpdateRequest {

    @JsonProperty("wikireview_id")
    @Schema(description = "Wiki 审核 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long wikireviewId;

    @JsonProperty("wiki_states")
    @Schema(description = "审核状态：1通过，2拒绝", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer wikiStates;
}
