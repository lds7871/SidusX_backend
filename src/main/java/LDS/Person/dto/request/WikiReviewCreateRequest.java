package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建 Wiki 审核请求 DTO
 */
@Getter
@Setter
@Schema(description = "创建 Wiki 审核请求")
public class WikiReviewCreateRequest {

    @JsonProperty("wiki_id")
    @Schema(description = "关联的 Wiki ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long wikiId;

    @JsonProperty("texts")
    @Schema(description = "Wiki 审核内容文本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String texts;

    @JsonProperty("tags")
    @Schema(description = "标签数组")
    private String[] tags;

    @JsonProperty("version")
    @Schema(description = "版本号")
    private Double version;

    @JsonProperty("update_user")
    @Schema(description = "更新用户 ID")
    private String updateUser;
}
