package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * API日志查询结果DTO - 返回最近20条记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "API日志查询结果（最近20条）")
public class ApiLogSimpleResultResponse {

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "响应消息", example = "✅ 日志查询成功")
    private String message;

    @Schema(description = "日志记录列表")
    private List<ApiLogResponse> data;

    @Schema(description = "时间戳", example = "1703332200000")
    private Long timestamp;
}
