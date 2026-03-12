package LDS.Person.service.impl;

import LDS.Person.dto.request.*;
import LDS.Person.dto.response.UserInfoResponse;
import LDS.Person.dto.response.GameAchievementResponse;
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
import java.util.Base64;

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

    /**
     * 处理cover字段的格式转换
     * 数据库中可能存在两种格式：
     * 1. 错误格式：Base64编码的数字序列字符串 "MjU1LDIxNi..." → 需要解码并转换
     * 2. 正确格式：直接的Base64编码 "/9j/4AAQSkZJRg..." → 直接使用
     */
    private String processCover(String coverFromDb) {
        if (coverFromDb == null || coverFromDb.isEmpty()) {
            return null;
        }

        try {
            // 检查是否是错误格式（被Base64编码的数字序列）
            if (coverFromDb.matches("^[A-Za-z0-9+/]+=*$")) {
                try {
                    String decoded = new String(Base64.getDecoder().decode(coverFromDb), StandardCharsets.UTF_8);
                    // 检查是否是数字序列格式
                    if (decoded.matches("^\\d{1,3}(,\\d{1,3})*$")) {
                        // 这是错误格式，需要转换
                        String[] numbers = decoded.split(",");
                        byte[] bytes = new byte[numbers.length];
                        for (int i = 0; i < numbers.length; i++) {
                            bytes[i] = (byte) Integer.parseInt(numbers[i]);
                        }
                        // 重新编码为正确的Base64
                        return Base64.getEncoder().encodeToString(bytes);
                    }
                } catch (Exception e) {
                    // 不是错误格式，直接返回原值
                    return coverFromDb;
                }
            }
            // 正确格式，直接返回
            return coverFromDb;
        } catch (Exception e) {
            logger.warn("处理cover字段出错: {}", e.getMessage());
            return coverFromDb;
        }
    }

    private UserInfoResponse toInfoResponse(User user) {
        UserInfoResponse resp = new UserInfoResponse();
        resp.setUserId(user.getUserId());
        resp.setName(user.getName());
        resp.setPhone(user.getPhone());
        resp.setMail(user.getMail());
        resp.setPlace(user.getPlace());
        resp.setAchievementJson(user.getAchievementJson());
        resp.setExpiredTime(user.getExpiredTime());
        // 处理cover字段，兼容两种格式
        if (user.getCover() != null && !user.getCover().isEmpty()) {
            resp.setCover(processCover(user.getCover()));
        }
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

    @Override
    public void updateCover(Long userId, String coverBase64) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或无效");
        }
        if (coverBase64 == null || coverBase64.isBlank()) {
            throw new IllegalArgumentException("头像不能为空");
        }

        // 验证Base64格式（基本检查）
        if (!coverBase64.matches("^[A-Za-z0-9+/]+=*$")) {
            throw new IllegalArgumentException("头像格式不正确，必须是Base64编码");
        }

        try {
            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }

            // 更新头像（MyBatis会使用 decode() 函数将Base64转换为bytea）
            int result = userMapper.updateCover(userId, coverBase64);
            if (result <= 0) {
                throw new RuntimeException("更新头像失败");
            }

            logger.info("用户头像更新成功 - userId: {}", userId);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("更新用户头像出错 - userId: {}", userId, e);
            throw new RuntimeException("更新头像失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateGameAchievement(UpdateGameAchievementRequest request) {
        Long userId = request.getUserid();
        String gameName = request.getGamename();
        Integer gameScore = request.getGamescore();

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或无效");
        }
        if (gameName == null || gameName.isBlank()) {
            throw new IllegalArgumentException("游戏名称不能为空");
        }
        if (gameScore == null) {
            throw new IllegalArgumentException("游戏分数不能为空");
        }

        try {
            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }

            // 获取当前的achievement_json
            String currentAchievementJson = user.getAchievementJson();
            if (currentAchievementJson == null || currentAchievementJson.isBlank()
                    || currentAchievementJson.equals("{}")) {
                currentAchievementJson = "{}";
            }

            // 使用fastjson2解析和更新JSON
            var achievementMap = JSON.parseObject(currentAchievementJson);
            achievementMap.put(gameName, gameScore);
            String updatedAchievementJson = JSON.toJSONString(achievementMap);

            // 更新数据库
            int result = userMapper.updateAchievementJson(userId, updatedAchievementJson);
            if (result <= 0) {
                throw new RuntimeException("更新成就失败");
            }

            logger.info("用户成就更新成功 - userId: {}, gameName: {}, gameScore: {}", userId, gameName, gameScore);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("更新用户成就出错 - userId: {}, gameName: {}", userId, gameName, e);
            throw new RuntimeException("更新成就失败: " + e.getMessage(), e);
        }
    }

    @Override
    public GameAchievementResponse getGameAchievement(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或无效");
        }

        try {
            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new IllegalArgumentException("用户不存在");
            }

            // 获取achievement_json
            String achievementJson = user.getAchievementJson();
            if (achievementJson == null || achievementJson.isBlank() || achievementJson.equals("{}")) {
                achievementJson = "{}";
            }

            // 解析JSON为对象
            Object achievements = JSON.parse(achievementJson);

            logger.info("用户成就获取成功 - userId: {}", userId);
            return GameAchievementResponse.builder()
                    .userId(userId)
                    .achievements(achievements)
                    .build();
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("获取用户成就出错 - userId: {}", userId, e);
            throw new RuntimeException("获取成就失败: " + e.getMessage(), e);
        }
    }
}
