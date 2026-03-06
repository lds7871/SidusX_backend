package LDS.Person.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Wiki留言实体类
 */
@Data
@Schema(description = "Wiki留言信息")
public class WikiComment {

  @Schema(description = "留言ID，自增主键", example = "1")
  private Long replyId;

  @Schema(description = "对应百科ID", example = "1")
  private Long wikiId;

  @Schema(description = "留言用户ID", example = "1")
  private Long userId;

  @Schema(description = "留言内容")
  private String text;

  @Schema(description = "点赞数量", example = "0")
  private Integer likes;

  @Schema(description = "留言时间")
  private LocalDateTime createTime;
}
