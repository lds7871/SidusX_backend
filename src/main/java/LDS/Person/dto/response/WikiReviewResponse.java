package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Wiki 审核响应 DTO
 */
@Getter
@Setter
@Schema(description = "Wiki 审核响应")
public class WikiReviewResponse {

    @JsonProperty("wikireview_id")
    @Schema(description = "Wiki 审核 ID")
    private Long wikireviewId;

    @JsonProperty("wiki_id")
    @Schema(description = "关联的 Wiki ID")
    private Long wikiId;

    @JsonProperty("texts")
    @Schema(description = "Wiki 审核内容文本")
    private String texts;

    @JsonProperty("tags")
    @Schema(description = "标签数组")
    private String[] tags;

    @JsonProperty("version")
    @Schema(description = "版本号")
    private Double version;

    @JsonProperty("update_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @JsonProperty("update_user")
    @Schema(description = "更新用户 ID")
    private String updateUser;

    @JsonProperty("wiki_states")
    @Schema(description = "审核状态：0待审核，1通过，2拒绝")
    private Integer wikiStates;
}
