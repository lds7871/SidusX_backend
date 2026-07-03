package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki 新增分页查询请求 DTO
 */
@Getter
@Setter
@Schema(description = "Wiki 新增分页查询请求")
public class WikiNewPageQueryRequest {

  @Schema(description = "当前页码", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer page = 1;

  @Schema(description = "每页数量（最大100）", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer pageSize = 10;

  @Schema(description = "审核状态（0：待审核，1：通过，2：拒绝）", example = "0")
  private Integer wikiStates;

  @Schema(description = "Wiki ID")
  private Long wikinewId;
}
