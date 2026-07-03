package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户操作通用响应结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户操作响应结果")
public class UserResultResponse {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "✅ 操作成功")
    private String message;

    @Schema(description = "响应数据")
    private Object data;

    @Schema(description = "时间戳", example = "1703332200000")
    private Long timestamp;
}
