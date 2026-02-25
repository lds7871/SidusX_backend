package LDS.Person.service.impl;

import LDS.Person.dto.request.*;
import LDS.Person.dto.response.UserInfoResponse;
import LDS.Person.entity.User;
import LDS.Person.repository.UserMapper;
import LDS.Person.service.EmailService;
import LDS.Person.service.UserService;
import LDS.Person.service.VerifyCodeStore;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    /** Session 中存储已登录用户ID的键名 */
    public static final String SESSION_USER_ID = "LOGIN_USER_ID";

    /** 登录有效期（小时） */
    private static final int LOGIN_EXPIRE_HOURS = 24;

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final VerifyCodeStore verifyCodeStore;

    public UserServiceImpl(UserMapper userMapper, EmailService emailService, VerifyCodeStore verifyCodeStore) {
        this.userMapper = userMapper;
        this.emailService = emailService;
        this.verifyCodeStore = verifyCodeStore;
    }

    // ===================== 密码加密 =====================

    /**
     * 将明文密码加密：文本 → UTF-8字节 → MD5哈希 → 16进制字符串
     */
    public static String encryptPassword(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不可用", e);
        }
    }

    // ===================== 工具方法 =====================

    private UserInfoResponse toInfoResponse(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setUserId(user.getUserId());
        resp.setName(user.getName());
        resp.setPhone(user.getPhone());
        resp.setMail(user.getMail());
        resp.setPlace(user.getPlace());
        resp.setAchievementJson(user.getAchievementJson());
        resp.setExpiredTime(user.getExpiredTime());
        return resp;
    }

    // ===================== 接口实现 =====================

    @Override
    public UserInfoResponse login(UserLoginRequest request, HttpSession session) {
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        User user = null;
        if (request.getMail() != null && !request.getMail().isBlank()) {
            user = userMapper.selectByMail(request.getMail().trim());
        } else if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user = userMapper.selectByPhone(request.getPhone().trim());
        } else {
            throw new IllegalArgumentException("邮箱或手机号不能为空");
        }

        if (user == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        String inputHash = encryptPassword(request.getPassword());
        if (!inputHash.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 更新登录过期时间
        LocalDateTime expiredTime = LocalDateTime.now().plusHours(LOGIN_EXPIRE_HOURS);
        userMapper.updateExpiredTime(user.getUserId(), expiredTime);
        user.setExpiredTime(expiredTime);

        // 将用户ID写入Session
        session.setAttribute(SESSION_USER_ID, user.getUserId());

        logger.info("用户登录成功 - userId: {}, mail: {}", user.getUserId(), user.getMail());
        return toInfoResponse(user);
    }

    @Override
    public void sendRegisterCode(UserRegisterSendCodeRequest request) {
        if (request.getMail() == null || request.getMail().isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("姓名不能为空");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        String mail = request.getMail().trim();

        // 检查邮箱是否已被注册
        if (userMapper.selectByMail(mail) != null) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }

        // 检查手机号是否已被注册
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (userMapper.selectByPhone(request.getPhone().trim()) != null) {
                throw new IllegalArgumentException("该手机号已被注册");
            }
        }

        // 缓存注册信息（密码在缓存前先加密）
        request.setPassword(encryptPassword(request.getPassword()));
        verifyCodeStore.storePendingRegister(mail, JSON.toJSONString(request));

        // 生成验证码并发送邮件
        String code = verifyCodeStore.generateAndStore(mail);
        String subject = "【账号注册验证码】";
        String content = "您正在注册账号，验证码为：" + code + "，有效期5分钟，请勿泄露。";
        emailService.sendSimpleMail(mail, subject, content);

        logger.info("注册验证码已发送 - mail: {}", mail);
    }

    @Override
    public UserInfoResponse confirmRegister(UserRegisterConfirmRequest request) {
        if (request.getMail() == null || request.getMail().isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (request.getVerifyCode() == null || request.getVerifyCode().isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }

        String mail = request.getMail().trim();

        if (!verifyCodeStore.verify(mail, request.getVerifyCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        String pendingJson = verifyCodeStore.getPendingRegister(mail);
        if (pendingJson == null) {
            throw new IllegalArgumentException("注册信息已失效，请重新发送验证码");
        }

        UserRegisterSendCodeRequest pending = JSON.parseObject(pendingJson, UserRegisterSendCodeRequest.class);

        // 创建用户实体（密码已在发送验证码时加密）
        User user = new User();
        user.setName(pending.getName());
        user.setMail(mail);
        user.setPhone(pending.getPhone());
        user.setPasswordHash(pending.getPassword());
        user.setPlace(pending.getPlace());
        user.setAchievementJson("{}");

        userMapper.insert(user);

        // 清除临时数据
        verifyCodeStore.remove(mail);
        verifyCodeStore.removePendingRegister(mail);

        logger.info("用户注册成功 - userId: {}, mail: {}", user.getUserId(), mail);
        return toInfoResponse(user);
    }

    @Override
    public void logout(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId != null) {
            // 将过期时间置为当前时间（立即失效）
            userMapper.updateExpiredTime((Long) userId, LocalDateTime.now());
            logger.info("用户登出 - userId: {}", userId);
        }
        session.invalidate();
    }

    @Override
    public void sendChangePasswordCode(ChangePasswordSendCodeRequest request) {
        if (request.getMail() == null || request.getMail().isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        String mail = request.getMail().trim();
        User user = userMapper.selectByMail(mail);
        if (user == null) {
            throw new IllegalArgumentException("该邮箱未注册");
        }

        String code = verifyCodeStore.generateAndStore(mail);
        String subject = "【修改密码验证码】";
        String content = "您正在修改账号密码，验证码为：" + code + "，有效期5分钟，请勿泄露。";
        emailService.sendSimpleMail(mail, subject, content);

        logger.info("修改密码验证码已发送 - mail: {}", mail);
    }

    @Override
    public void changePassword(ChangePasswordConfirmRequest request) {
        if (request.getMail() == null || request.getMail().isBlank()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (request.getVerifyCode() == null || request.getVerifyCode().isBlank()) {
            throw new IllegalArgumentException("验证码不能为空");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("新密码不能为空");
        }

        String mail = request.getMail().trim();

        if (!verifyCodeStore.verify(mail, request.getVerifyCode())) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        User user = userMapper.selectByMail(mail);
        if (user == null) {
            throw new IllegalArgumentException("该邮箱未注册");
        }

        String newHash = encryptPassword(request.getNewPassword());
        userMapper.updatePasswordHash(user.getUserId(), newHash);

        // 移除验证码（一次性）
        verifyCodeStore.remove(mail);

        logger.info("密码修改成功 - userId: {}, mail: {}", user.getUserId(), mail);
    }
}
