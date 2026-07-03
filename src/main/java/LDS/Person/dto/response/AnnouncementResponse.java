package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "公告响应对象")
public class AnnouncementResponse {

  @Schema(description = "公告ID", example = "1")
  private Long annId;

  @Schema(description = "公告内容")
  private String content;

  @Schema(description = "创建时间")
  private LocalDateTime createTime;
}
