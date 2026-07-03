namespace SidusX.Config;

/// <summary>安全配置</summary>
public class SecuritySettings
{
    public bool IpWhitelistEnabled { get; set; } = false;
    public List<string> IpWhitelist { get; set; } = new();
    public bool PassTokenEnabled { get; set; } = false;
    public string PassTokens { get; set; } = string.Empty;

    public HashSet<string> GetPassTokenSet()
    {
        if (string.IsNullOrWhiteSpace(PassTokens)) return new HashSet<string>();
        return PassTokens.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries)
                         .ToHashSet();
    }
}

/// <summary>代理配置</summary>
public class ProxySettings
{
    public bool IsOpen { get; set; } = false;
    public string Host { get; set; } = "127.0.0.1";
    public int Port { get; set; } = 33210;
    public int SocksPort { get; set; } = 33211;
}

/// <summary>邮件配置</summary>
public class MailSettings
{
    public SmtpSettings Smtp { get; set; } = new();

    public class SmtpSettings
    {
        public string Host { get; set; } = string.Empty;
        public int Port { get; set; } = 587;
        public string Username { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public string From { get; set; } = string.Empty;
        public int Timeout { get; set; } = 5000;
        public bool UseSsl { get; set; } = false;
        public bool UseStartTls { get; set; } = true;
    }
}

/// <summary>WebSocket控制台配置</summary>
public class WebSocketConsoleSettings
{
    public bool Enabled { get; set; } = true;
    public string Endpoint { get; set; } = "/ws/console";
    public string Password { get; set; } = string.Empty;
    public int MaxTextMessageBufferSize { get; set; } = 512000;
    public int MaxBinaryMessageBufferSize { get; set; } = 512000;
    public int MessageQueueSize { get; set; } = 1000;
    public int HistoryBufferSize { get; set; } = 100;
}

/// <summary>Session配置</summary>
public class SessionSettings
{
    public int IdleTimeout { get; set; } = 1800;
    public string CookieName { get; set; } = "JSESSIONID";
    public bool HttpOnly { get; set; } = true;
    public bool Secure { get; set; } = false;
}

/// <summary>应用配置</summary>
public class ApplicationSettings
{
    public string Name { get; set; } = "SidusX_backend";
    public int Port { get; set; } = 8100;
}
