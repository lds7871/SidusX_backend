package LDS.Person.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码发送验证码请求DTO
 */
@Data
@NoArgsConstructor
@Schema(description = "修改密码 - 发送验证码请求")
public class ChangePasswordSendCodeRequest {

    @Schema(description = "账户绑定邮箱", example = "user@example.com", required = true)
    private String mail;
}
