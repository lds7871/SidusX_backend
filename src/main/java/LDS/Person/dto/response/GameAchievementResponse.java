package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 游戏成就响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "游戏成就响应")
public class GameAchievementResponse {

  @Schema(description = "用户ID", example = "3")
  private Long userId;

  @Schema(description = "成就JSON数据", example = "{\"Game1\": 100, \"Game2_空间站对接.vue\": 935}")
  private Object achievements;
}
