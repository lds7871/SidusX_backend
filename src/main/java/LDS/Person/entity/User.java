package LDS.Person.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Schema(description = "用户信息")
public class User {

    @Schema(description = "用户ID，自增主键", example = "1")
    private Long userId;

    @Schema(description = "用户姓名", example = "张三")
    private String name;

    @Schema(description = "用户头像二进制数据")
    private byte[] cover;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "邮箱", example = "user@example.com")
    private String mail;

    @Schema(description = "用户密码哈希")
    private String passwordHash;

    @Schema(description = "用户所在地区", example = "上海")
    private String place;

    @Schema(description = "成就 JSON 数据")
    private String achievementJson;

    @Schema(description = "登录过期时间")
    private LocalDateTime expiredTime;
}
