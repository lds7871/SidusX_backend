package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "用户登录请求")
public class UserLoginRequest {

    @Schema(description = "邮箱（与手机号二选一）", example = "user@example.com")
    private String mail;

    @Schema(description = "手机号（与邮箱二选一）", example = "13800138000")
    private String phone;

    @Schema(description = "密码（明文）", example = "MyPassword123", required = true)
    private String password;
}
