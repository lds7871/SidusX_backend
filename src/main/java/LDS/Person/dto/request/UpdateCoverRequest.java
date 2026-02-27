package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改用户头像请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "修改用户头像请求")
public class UpdateCoverRequest {

  @Schema(description = "用户ID", example = "1", required = true)
  private Long userId;

  @Schema(description = "用户头像（Base64编码）", required = true)
  private String cover;
}
