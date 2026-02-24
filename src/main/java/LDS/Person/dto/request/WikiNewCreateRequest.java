package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki 新增请求 DTO
 * 用于创建新的 Wiki 审核申请
 */
@Getter
@Setter
@Schema(description = "Wiki 新增请求")
public class WikiNewCreateRequest {

  @Schema(description = "Wiki 键名（唯一）", example = "java_basics", requiredMode = Schema.RequiredMode.REQUIRED)
  private String keyName;

  @Schema(description = "Wiki 内容文本", example = "Java 基础教程内容", requiredMode = Schema.RequiredMode.REQUIRED)
  private String texts;

  @Schema(description = "标签数组", example = "[\"java\", \"programming\"]")
  private String[] tags;

  @Schema(description = "创建用户（同时作为更新用户）", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
  private String createUser;
}
