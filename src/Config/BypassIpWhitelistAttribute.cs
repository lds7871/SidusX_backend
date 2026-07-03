namespace SidusX.Config;

/// <summary>
/// 标记Controller方法可绕过IP白名单限制（公开接口）
/// </summary>
[AttributeUsage(AttributeTargets.Method | AttributeTargets.Class, AllowMultiple = false)]
public class BypassIpWhitelistAttribute : Attribute
{
    public string Reason { get; }

    public BypassIpWhitelistAttribute(string reason = "")
    {
        Reason = reason;
    }
}
