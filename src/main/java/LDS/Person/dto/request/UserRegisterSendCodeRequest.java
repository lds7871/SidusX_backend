package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册发送验证码请求DTO
 * 提交后会向邮箱发送5分钟有效的验证码
 */
@Data
@NoArgsConstructor
@Schema(description = "用户注册 - 发送验证码请求")
public class UserRegisterSendCodeRequest {

    @Schema(description = "用户姓名", example = "张三", required = true)
    private String name;

    @Schema(description = "邮箱（接收验证码）", example = "user@example.com", required = true)
    private String mail;

    @Schema(description = "手机号（可选）", example = "13800138000")
    private String phone;

    @Schema(description = "密码（明文）", example = "MyPassword123", required = true)
    private String password;

    @Schema(description = "用户所在地区（可选）", example = "上海")
    private String place;
}
