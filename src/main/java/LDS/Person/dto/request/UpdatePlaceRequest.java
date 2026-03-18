package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户地区请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "更新用户地区请求")
public class UpdatePlaceRequest {

  @Schema(description = "用户ID", example = "1", required = true)
  private Long userId;

  @Schema(description = "用户所在地区", example = "114.514", required = true)
  private String place;
}
