package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API日志查询请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "API日志分页查询请求")
public class ApiLogQueryRequest {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "HTTP状态码，可选", example = "200")
    private Integer states;

    @Schema(description = "开始时间，格式：yyyy-MM-dd HH:mm:ss", example = "2025-12-20 00:00:00")
    private String startTime;

    @Schema(description = "结束时间，格式：yyyy-MM-dd HH:mm:ss", example = "2025-12-24 23:59:59")
    private String endTime;
}
