package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册确认请求DTO（提交邮箱验证码完成注册）
 */
@Data
@NoArgsConstructor
@Schema(description = "用户注册 - 验证码确认请求")
public class UserRegisterConfirmRequest {

    @Schema(description = "邮箱", example = "user@example.com", required = true)
    private String mail;

    @Schema(description = "邮箱验证码（6位数字）", example = "123456", required = true)
    private String verifyCode;
}
