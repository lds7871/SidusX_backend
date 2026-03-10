package LDS.Person.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮件发送服务类
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Value("${mail.smtp.from}")
    private String from;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * 发送简单文本邮件
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件主题
     * @param text    邮件内容（纯文本）
     */
    public void sendSimpleMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
            log.info("简单邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (Exception e) {
            log.error("发送简单邮件失败 - 收件人: {}, 错误信息: {}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送 HTML 格式邮件
     *
     * @param to          收件人邮箱地址
     * @param subject     邮件主题
     * @param htmlContent 邮件内容（HTML格式）
     */
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            log.info("HTML邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            log.error("发送HTML邮件失败 - 收件人: {}, 错误信息: {}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 发送带附件的邮件
     *
     * @param to          收件人邮箱地址
     * @param subject     邮件主题
     * @param htmlContent 邮件内容（HTML格式）
     * @param attachments 附件信息（文件名 -> 文件路径）
     */
    public void sendMailWithAttachment(String to, String subject, String htmlContent,
            java.util.Map<String, String> attachments) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            // 添加附件
            if (attachments != null && !attachments.isEmpty()) {
                for (String fileName : attachments.keySet()) {
                    String filePath = attachments.get(fileName);
                    helper.addAttachment(fileName, new java.io.File(filePath));
                }
            }

            javaMailSender.send(message);
            log.info("带附件邮件发送成功 - 收件人: {}, 主题: {}", to, subject);
        } catch (MessagingException e) {
            log.error("发送带附件邮件失败 - 收件人: {}, 错误信息: {}", to, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    /**
     * 批量发送邮件
     *
     * @param recipients 收件人邮箱列表
     * @param subject    邮件主题
     * @param text       邮件内容
     */
    public void sendBatchMail(java.util.List<String> recipients, String subject, String text) {
        for (String to : recipients) {
            try {
                sendSimpleMail(to, subject, text);
            } catch (Exception e) {
                log.error("批量邮件发送失败 - 收件人: {}", to, e);
            }
        }
    }
}
