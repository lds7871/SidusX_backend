package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki 新增审核请求 DTO
 * 用于批准或驳回 Wiki 新增申请
 */
@Getter
@Setter
@Schema(description = "Wiki 新增审核请求")
public class WikiNewReviewRequest {

  @Schema(description = "Wiki 新增 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long wikinewId;

  @Schema(description = "审核状态（1: 批准, 2: 驳回）", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer wikiStates;

  @Schema(description = "审核用户（可选）", example = "admin")
  private String reviewUser;

  @Schema(description = "审核备注（可选）", example = "内容符合要求")
  private String remarks;
}
