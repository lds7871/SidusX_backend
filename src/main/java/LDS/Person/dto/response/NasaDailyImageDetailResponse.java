package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NASA 每日图片详细响应类
 * 返回 apod_id, copyright, explanation, media_type, title, url, create_time
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "NASA 每日图片详细响应")
public class NasaDailyImageDetailResponse {

  @Schema(description = "图片ID", example = "1")
  private Long apodId;

  @Schema(description = "图片大标题（版权信息）", example = "NASA")
  private String copyright;

  @Schema(description = "图片说明文字", example = "This is a detailed explanation of the image...")
  private String explanation;

  @Schema(description = "媒体类型（image 或 video）", example = "image")
  private String mediaType;

  @Schema(description = "图片标题", example = "The Pillars of Creation")
  private String title;

  @Schema(description = "图片或视频的链接", example = "https://apod.nasa.gov/apod/image/2501/...")
  private String url;

  @Schema(description = "创建时间", example = "2025-01-05 10:30:00")
  private String createTime;
}
