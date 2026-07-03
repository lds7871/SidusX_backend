package LDS.Person.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 邮箱验证码内存存储服务
 * 验证码有效期为5分钟，同一邮箱新发送的验证码会覆盖旧的
 */
@Service
public class VerifyCodeStore {

    /** 验证码有效期（分钟） */
    private static final int EXPIRE_MINUTES = 5;

    private record CodeEntry(String code, LocalDateTime expireAt) {}

    /**
     * key=邮箱, value=验证码信息
     * 对于注册场景还需暂存待创建的用户数据，因此提供两套存储
     */
    private final Map<String, CodeEntry> codeMap = new ConcurrentHashMap<>();

    /** 注册场景：同时缓存待注册用户信息（JSON序列化为字符串） */
    private final Map<String, String> pendingRegisterMap = new ConcurrentHashMap<>();

    /**
     * 生成并存储6位数字验证码
     *
     * @param mail 收件人邮箱
     * @return 生成的验证码
     */
    public String generateAndStore(String mail) {
        String code = String.format("%06d", new Random().nextInt(1_000_000));
        codeMap.put(mail, new CodeEntry(code, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));
        return code;
    }

    /**
     * 验证验证码是否正确且未过期
     *
     * @param mail 邮箱
     * @param code 用户输入的验证码
     * @return true=验证通过
     */
    public boolean verify(String mail, String code) {
        CodeEntry entry = codeMap.get(mail);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expireAt())) {
            codeMap.remove(mail);
            return false;
        }
        return entry.code().equals(code);
    }

    /**
     * 验证通过后移除验证码（一次性使用）
     *
     * @param mail 邮箱
     */
    public void remove(String mail) {
        codeMap.remove(mail);
    }

    // ---- 注册待提交数据 ----

    public void storePendingRegister(String mail, String userDataJson) {
        pendingRegisterMap.put(mail, userDataJson);
    }

    public String getPendingRegister(String mail) {
        return pendingRegisterMap.get(mail);
    }

    public void removePendingRegister(String mail) {
        pendingRegisterMap.remove(mail);
    }
}
