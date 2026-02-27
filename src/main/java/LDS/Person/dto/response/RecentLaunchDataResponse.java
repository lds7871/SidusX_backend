package LDS.Person.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 最近发射数据响应 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "最近发射数据响应")
public class RecentLaunchDataResponse {

  @Schema(description = "记录 ID", example = "5")
  private Long id;

  @Schema(description = "完整发射数据（JSONB 原始内容）")
  private JsonNode data;

  @Schema(description = "数据获取时间", example = "2026-02-27 02:10:00")
  private String getTime;
}
