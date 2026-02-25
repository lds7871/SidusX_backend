package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki 新增审核响应 DTO
 */
@Getter
@Setter
@Schema(description = "Wiki 新增审核响应")
public class WikiNewReviewResponse {

  @Schema(description = "Wiki 新增 ID")
  private Long wikinewId;

  @Schema(description = "审核状态（1: 批准, 2: 驳回）")
  private Integer wikiStates;

  @Schema(description = "审核结果提示信息")
  private String message;

  @Schema(description = "如果批准，返回生成的 Wiki ID")
  private Long generatedWikiId;

  public WikiNewReviewResponse() {
  }

  public WikiNewReviewResponse(Long wikinewId, Integer wikiStates, String message) {
    this.wikinewId = wikinewId;
    this.wikiStates = wikiStates;
    this.message = message;
  }

  public WikiNewReviewResponse(Long wikinewId, Integer wikiStates, String message, Long generatedWikiId) {
    this.wikinewId = wikinewId;
    this.wikiStates = wikiStates;
    this.message = message;
    this.generatedWikiId = generatedWikiId;
  }
}
