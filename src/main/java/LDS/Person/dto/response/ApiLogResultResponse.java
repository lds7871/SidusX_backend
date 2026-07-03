package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API通用响应结果DTO - 用于API日志查询
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API响应结果")
public class ApiLogResultResponse {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "✅ 日志查询成功")
    private String message;

    @Schema(description = "响应数据")
    private ApiLogPageResponse data;

    @Schema(description = "时间戳", example = "1703332200000")
    private Long timestamp;
}
