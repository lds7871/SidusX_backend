package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新游戏成就请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "更新游戏成就请求")
public class UpdateGameAchievementRequest {

  @Schema(description = "用户ID", example = "3", required = true)
  private Long userid;

  @Schema(description = "游戏名称", example = "Game2_空间站对接.vue", required = true)
  private String gamename;

  @Schema(description = "游戏分数", example = "935", required = true)
  private Integer gamescore;
}
