package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最新更新的 Wiki 摘要
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "最新更新的 Wiki 摘要")
public class LatestWikiSummaryResponse {

  @Schema(description = "Wiki ID", example = "1")
  @JsonProperty("wiki_id")
  private Long wikiId;

  @Schema(description = "Wiki 键名", example = "java_basics")
  @JsonProperty("key_name")
  private String keyName;

  @Schema(description = "标签列表")
  @JsonProperty("tags")
  private String[] tags;

  @Schema(description = "版本号", example = "1.3")
  @JsonProperty("version")
  private Double version;

  @Schema(description = "更新人名称")
  @JsonProperty("update_user")
  private String updateUser;
}
