package LDS.Person.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * 邮箱配置类 - 配置 SMTP 邮件发送器
 */
@Configuration
public class EmailConfig {

  @Value("${mail.smtp.host}")
  private String host;

  @Value("${mail.smtp.port}")
  private int port;

  @Value("${mail.smtp.username}")
  private String username;

  @Value("${mail.smtp.password}")
  private String password;

  @Value("${mail.smtp.protocol:smtp}")
  private String protocol;

  @Value("${mail.smtp.timeout:5000}")
  private int timeout;

  /**
   * 配置 JavaMailSender Bean
   */
  @Bean
  public JavaMailSender javaMailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

    // 基本配置
    mailSender.setHost(host);
    mailSender.setPort(port);
    mailSender.setUsername(username);
    mailSender.setPassword(password);
    mailSender.setProtocol(protocol);
    mailSender.setDefaultEncoding("UTF-8");

    // SMTP 属性配置
    Properties properties = new Properties();
    properties.setProperty("mail.smtp.auth", "true");
    properties.setProperty("mail.smtp.starttls.enable", "true");
    properties.setProperty("mail.smtp.starttls.required", "true");
    properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    properties.setProperty("mail.smtp.timeout", String.valueOf(timeout));
    properties.setProperty("mail.smtp.connectiontimeout", String.valueOf(timeout));

    mailSender.setJavaMailProperties(properties);

    return mailSender;
  }
}
