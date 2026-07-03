package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API日志查询请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "API日志查询请求")
public class ApiLogQueryRequest {

    @Schema(description = "HTTP状态码，可选", example = "200")
    private Integer states;

    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss，可选", example = "2025-12-20 00:00:00")
    private String startTime;

    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss，可选", example = "2025-12-24 23:59:59")
    private String endTime;
}
