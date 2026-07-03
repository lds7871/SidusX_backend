using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;
using SidusX.Config;

namespace SidusX.Utils;

public class EmailService
{
    private readonly ILogger<EmailService> _logger;
    private readonly MailSettings _settings;

    public EmailService(ILogger<EmailService> logger, IOptions<MailSettings> settings)
    {
        _logger = logger;
        _settings = settings.Value;
    }

    public async Task SendSimpleMailAsync(string to, string subject, string text)
    {
        var message = new MimeMessage();
        message.From.Add(MailboxAddress.Parse(_settings.Smtp.From));
        message.To.Add(MailboxAddress.Parse(to));
        message.Subject = subject;
        message.Body = new TextPart("plain") { Text = text };

        await SendAsync(message);
        _logger.LogInformation("邮件发送成功 - 收件人: {To}, 主题: {Subject}", to, subject);
    }

    public void SendSimpleMail(string to, string subject, string text)
    {
        Task.Run(() => SendSimpleMailAsync(to, subject, text)).GetAwaiter().GetResult();
    }

    private async Task SendAsync(MimeMessage message)
    {
        using var client = new SmtpClient();
        var securOpt = _settings.Smtp.UseSsl ? SecureSocketOptions.SslOnConnect
            : _settings.Smtp.UseStartTls ? SecureSocketOptions.StartTls
            : SecureSocketOptions.None;

        await client.ConnectAsync(_settings.Smtp.Host, _settings.Smtp.Port, securOpt);
        await client.AuthenticateAsync(_settings.Smtp.Username, _settings.Smtp.Password);
        await client.SendAsync(message);
        await client.DisconnectAsync(true);
    }
}
