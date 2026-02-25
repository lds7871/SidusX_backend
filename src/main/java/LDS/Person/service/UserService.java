package LDS.Person.service;

import LDS.Person.dto.request.*;
import LDS.Person.dto.response.UserInfoResponse;

import jakarta.servlet.http.HttpSession;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     * 验证账号密码，成功后将用户ID写入Session并更新DB中的过期时间
     *
     * @param request 登录请求（mail 或 phone + password）
     * @param session HTTP Session
     * @return 用户信息
     */
    UserInfoResponse login(UserLoginRequest request, HttpSession session);

    /**
     * 注册第一步：发送邮箱验证码
     * 校验邮箱/手机号是否已被占用，通过后发送5分钟有效验证码
     *
     * @param request 注册信息（含邮箱、姓名、密码等）
     */
    void sendRegisterCode(UserRegisterSendCodeRequest request);

    /**
     * 注册第二步：验证验证码并创建用户
     *
     * @param request 验证码确认请求
     * @return 新建用户信息
     */
    UserInfoResponse confirmRegister(UserRegisterConfirmRequest request);

    /**
     * 用户登出
     * 清除Session并将DB中的过期时间置为当前时间
     *
     * @param session HTTP Session
     */
    void logout(HttpSession session);

    /**
     * 修改密码第一步：向邮箱发送验证码
     *
     * @param request 包含邮箱的请求
     */
    void sendChangePasswordCode(ChangePasswordSendCodeRequest request);

    /**
     * 修改密码第二步：验证验证码并更新密码
     *
     * @param request 包含邮箱、验证码、新密码的请求
     */
    void changePassword(ChangePasswordConfirmRequest request);
}
