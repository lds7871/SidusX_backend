namespace SidusX.Utils;

/// <summary>
/// 邮箱验证码内存存储服务（验证码有效期5分钟）
/// </summary>
public class VerifyCodeStore
{
    private const int ExpireMinutes = 5;

    private record CodeEntry(string Code, DateTime ExpireAt);

    private readonly Dictionary<string, CodeEntry> _codeMap = new();
    private readonly Dictionary<string, string> _pendingRegisterMap = new();
    private readonly object _lock = new();

    public string GenerateAndStore(string mail)
    {
        var code = Random.Shared.Next(1_000_000).ToString("D6");
        lock (_lock)
        {
            _codeMap[mail] = new CodeEntry(code, DateTime.Now.AddMinutes(ExpireMinutes));
        }
        return code;
    }

    public bool Verify(string mail, string code)
    {
        lock (_lock)
        {
            if (!_codeMap.TryGetValue(mail, out var entry)) return false;
            if (DateTime.Now > entry.ExpireAt) { _codeMap.Remove(mail); return false; }
            return entry.Code == code;
        }
    }

    public void Remove(string mail)
    {
        lock (_lock) { _codeMap.Remove(mail); }
    }

    public void StorePendingRegister(string mail, string userDataJson)
    {
        lock (_lock) { _pendingRegisterMap[mail] = userDataJson; }
    }

    public string? GetPendingRegister(string mail)
    {
        lock (_lock) { return _pendingRegisterMap.GetValueOrDefault(mail); }
    }

    public void RemovePendingRegister(string mail)
    {
        lock (_lock) { _pendingRegisterMap.Remove(mail); }
    }
}
