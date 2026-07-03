package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API日志响应DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "API日志信息")
public class ApiLogResponse {

    @Schema(description = "主键ID", example = "1")
    private Integer id;

    @Schema(description = "访问者IP地址", example = "192.168.1.1")
    private String ip;

    @Schema(description = "访问路径", example = "/api/user/list")
    private String api;

    @Schema(description = "HTTP状态码", example = "200")
    private Integer states;

    @Schema(description = "访问时间")
    private LocalDateTime createTime;
}
