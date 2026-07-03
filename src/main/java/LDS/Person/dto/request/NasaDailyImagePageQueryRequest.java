package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * NASA 每日图片分页查询请求类
 */
@Getter
@Setter
@Schema(description = "NASA 每日图片分页查询请求")
public class NasaDailyImagePageQueryRequest {

  @Schema(description = "当前页码，默认为1", example = "1")
  private Integer page;

  @Schema(description = "每页数量，默认为10，最大为100", example = "10")
  private Integer pageSize;

  @Schema(description = "图片标题（模糊匹配，可选）", example = "Galaxy")
  private String title;

  @Schema(description = "创建时间开始（ISO 8601格式，可选）", example = "2025-01-01 00:00:00")
  private String createTimeStart;

  @Schema(description = "创建时间结束（ISO 8601格式，可选）", example = "2025-12-31 23:59:59")
  private String createTimeEnd;
}
