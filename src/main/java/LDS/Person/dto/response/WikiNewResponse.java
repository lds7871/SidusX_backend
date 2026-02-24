package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki 新增响应 DTO
 */
@Getter
@Setter
@Schema(description = "Wiki 新增响应")
public class WikiNewResponse {

  @Schema(description = "Wiki ID")
  private Long wikinewId;

  @Schema(description = "Wiki 键名")
  private String keyName;

  @Schema(description = "Wiki 内容文本")
  private String texts;

  @Schema(description = "标签数组")
  private String[] tags;

  @Schema(description = "版本号")
  private Double version;

  @Schema(description = "创建时间")
  private String createTime;

  @Schema(description = "创建用户")
  private String createUser;

  @Schema(description = "更新时间")
  private String updateTime;

  @Schema(description = "更新用户")
  private String updateUser;

  @Schema(description = "审核状态（0：待审核，1：通过，2：拒绝）")
  private Integer wikiStates;
}
