package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MS_SHIP 响应 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "MS_SHIP 响应")
public class MsShipResponse {

  @Schema(description = "MS_SHIP 记录ID", example = "1")
  private Long msId;

  @Schema(description = "JSON 内容", example = "{\"name\": \"Ship Name\", \"type\": \"missile\"}")
  private String content;
}
