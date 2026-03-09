package LDS.Person.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "系统公告实体")
public class Announcement {

  @Schema(description = "公告ID", example = "1")
  private Long annId;

  @Schema(description = "公告内容", requiredMode = Schema.RequiredMode.REQUIRED)
  private String content;

  @Schema(description = "创建时间")
  private LocalDateTime createTime;
}
