package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

/**
 * Wiki 新增列表响应 DTO
 * 用于分页查询返回的列表项
 */
@Getter
@Setter
@Schema(description = "Wiki 新增列表项响应")
public class WikiNewListResponse {

  @Schema(description = "Wiki ID")
  private Long wikinewId;

  @Schema(description = "Wiki 键名")
  private String keyName;

  @Schema(description = "标签数组")
  private List<String> tags;

  @Schema(description = "创建用户")
  private String createUser;

  @Schema(description = "审核状态（0：待审核，1：通过，2：拒绝）")
  private Integer wikiStates;
}
