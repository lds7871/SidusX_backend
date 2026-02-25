package LDS.Person.controller;

import LDS.Person.config.BypassIpWhitelist;
import LDS.Person.dto.request.*;
import LDS.Person.dto.response.UserInfoResponse;
import LDS.Person.dto.response.UserResultResponse;
import LDS.Person.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 提供登录、注册、登出、修改密码等接口
 */
@RestController
@RequestMapping("/GHapi/user")
@Tag(name = "用户管理", description = "用户登录、注册、登出、修改密码接口")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    // ==================== 登录 ====================

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @BypassIpWhitelist(reason = "公开接口 - 用户登录")
    @Operation(summary = "用户登录", description = "使用邮箱或手机号 + 密码登录，成功后写入Session并返回用户信息")
    public ResponseEntity<UserResultResponse> login(@RequestBody UserLoginRequest request,
                                                    HttpSession session) {
        try {
            UserInfoResponse info = userService.login(request, session);
            return ResponseEntity.ok(UserResultResponse.builder()
                    .code(200)
                    .message("✅ 登录成功")
                    .data(info)
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (IllegalArgumentException e) {
            logger.warn("用户登录失败: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UserResultResponse.builder()
                    .code(401)
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            logger.error("用户登录异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserResultResponse.builder()
                    .code(500)
                    .message("登录失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    // ==================== 注册 ====================

    /**
     * 注册第一步：发送邮箱验证码
     */
    @PostMapping("/register/sendCode")
    @BypassIpWhitelist(reason = "公开接口 - 用户注册发送验证码")
    @Operation(summary = "注册 - 发送验证码", description = "校验注册信息并向邮箱发送5分钟有效的验证码")
    public ResponseEntity<UserResultResponse> registerSendCode(@RequestBody UserRegisterSendCodeRequest request) {
        try {
            userService.sendRegisterCode(request);
            return ResponseEntity.ok(UserResultResponse.builder()
                    .code(200)
                    .message("✅ 验证码已发送，请在5分钟内完成验证")
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (IllegalArgumentException e) {
            logger.warn("发送注册验证码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(UserResultResponse.builder()
                    .code(400)
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            logger.error("发送注册验证码异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserResultResponse.builder()
                    .code(500)
                    .message("发送失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    /**
     * 注册第二步：提交验证码完成注册
     */
    @PostMapping("/register/confirm")
    @BypassIpWhitelist(reason = "公开接口 - 用户注册确认")
    @Operation(summary = "注册 - 验证码确认", description = "提交邮箱验证码完成账号注册")
    public ResponseEntity<UserResultResponse> registerConfirm(@RequestBody UserRegisterConfirmRequest request) {
        try {
            UserInfoResponse info = userService.confirmRegister(request);
            return ResponseEntity.ok(UserResultResponse.builder()
                    .code(200)
                    .message("✅ 注册成功")
                    .data(info)
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (IllegalArgumentException e) {
            logger.warn("注册确认失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(UserResultResponse.builder()
                    .code(400)
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            logger.error("注册确认异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserResultResponse.builder()
                    .code(500)
                    .message("注册失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    // ==================== 登出 ====================

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除Session并更新DB中的登录过期时间")
    public ResponseEntity<UserResultResponse> logout(HttpSession session) {
        try {
            userService.logout(session);
            return ResponseEntity.ok(UserResultResponse.builder()
                    .code(200)
                    .message("✅ 登出成功")
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            logger.error("用户登出异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserResultResponse.builder()
                    .code(500)
                    .message("登出失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    // ==================== 修改密码 ====================

    /**
     * 修改密码第一步：发送邮箱验证码
     */
    @PostMapping("/password/sendCode")
    @BypassIpWhitelist(reason = "公开接口 - 修改密码发送验证码")
    @Operation(summary = "修改密码 - 发送验证码", description = "向绑定邮箱发送5分钟有效的验证码")
    public ResponseEntity<UserResultResponse> passwordSendCode(@RequestBody ChangePasswordSendCodeRequest request) {
        try {
            userService.sendChangePasswordCode(request);
            return ResponseEntity.ok(UserResultResponse.builder()
                    .code(200)
                    .message("✅ 验证码已发送，请在5分钟内完成验证")
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (IllegalArgumentException e) {
            logger.warn("发送修改密码验证码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(UserResultResponse.builder()
                    .code(400)
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            logger.error("发送修改密码验证码异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserResultResponse.builder()
                    .code(500)
                    .message("发送失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }

    /**
     * 修改密码第二步：提交验证码和新密码
     */
    @PostMapping("/password/confirm")
    @BypassIpWhitelist(reason = "公开接口 - 修改密码确认")
    @Operation(summary = "修改密码 - 验证码确认", description = "提交邮箱验证码和新密码完成密码修改")
    public ResponseEntity<UserResultResponse> passwordConfirm(@RequestBody ChangePasswordConfirmRequest request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok(UserResultResponse.builder()
                    .code(200)
                    .message("✅ 密码修改成功")
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (IllegalArgumentException e) {
            logger.warn("修改密码失败: {}", e.getMessage());
            return ResponseEntity.badRequest().body(UserResultResponse.builder()
                    .code(400)
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            logger.error("修改密码异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(UserResultResponse.builder()
                    .code(500)
                    .message("修改失败: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build());
        }
    }
}
