package LDS.Person.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO（不含敏感字段）
 */
@Data
@NoArgsConstructor
@Schema(description = "用户信息")
public class UserInfoResponse {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String name;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "user@example.com")
    private String mail;

    @Schema(description = "用户所在地区", example = "上海")
    private String place;

    @Schema(description = "成就 JSON 数据")
    private String achievementJson;

    @Schema(description = "登录过期时间")
    private LocalDateTime expiredTime;

    @Schema(description = "用户头像（Base64编码）")
    private String cover;
}
