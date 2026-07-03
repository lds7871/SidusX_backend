package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki留言查询请求 DTO
 * 通过wiki_id查询所有留言
 */
@Getter
@Setter
@Schema(description = "Wiki留言查询请求")
public class WikiCommentByWikiIdRequest {

  @JsonProperty("wiki_id")
  @Schema(description = "Wiki ID", example = "1", required = true)
  private Long wikiId;
}
