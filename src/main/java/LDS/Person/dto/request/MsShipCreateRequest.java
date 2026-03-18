package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * MS_SHIP 创建请求 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MS_SHIP 创建请求")
public class MsShipCreateRequest {

  @Schema(description = "JSON 内容，支持数组或对象格式", example = "{\"name\": \"Ship Name\", \"type\": \"missile\"}")
  private String content;
}
