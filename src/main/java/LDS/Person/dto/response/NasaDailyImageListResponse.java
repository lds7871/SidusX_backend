package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NASA 每日图片列表响应类（分页查询）
 * 只返回 apod_id, title, create_time
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "NASA 每日图片列表响应")
public class NasaDailyImageListResponse {

  @Schema(description = "图片ID", example = "1")
  private Long apodId;

  @Schema(description = "图片标题", example = "The Pillars of Creation")
  private String title;

  @Schema(description = "创建时间", example = "2025-01-05 10:30:00")
  private String createTime;
}
