package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki留言创建请求 DTO
 */
@Getter
@Setter
@Schema(description = "Wiki留言创建请求")
public class WikiCommentCreateRequest {

  @JsonProperty("wiki_id")
  @Schema(description = "Wiki ID", example = "1", required = true)
  private Long wikiId;

  @JsonProperty("user_id")
  @Schema(description = "用户 ID", example = "1", required = true)
  private Long userId;

  @JsonProperty("text")
  @Schema(description = "留言内容", example = "这是一条留言", required = true)
  private String text;
}