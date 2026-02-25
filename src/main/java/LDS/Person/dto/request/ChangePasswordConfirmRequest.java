package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码确认请求DTO（提交验证码和新密码）
 */
@Data
@NoArgsConstructor
@Schema(description = "修改密码 - 验证码确认请求")
public class ChangePasswordConfirmRequest {

    @Schema(description = "账户绑定邮箱", example = "user@example.com", required = true)
    private String mail;

    @Schema(description = "邮箱验证码（6位数字）", example = "123456", required = true)
    private String verifyCode;

    @Schema(description = "新密码（明文）", example = "NewPassword456", required = true)
    private String newPassword;
}
