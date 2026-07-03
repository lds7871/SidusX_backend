using System.Security.Cryptography;
using System.Text;

namespace SidusX.Utils;

public static class PasswordHelper
{
    /// <summary>将明文密码加密为MD5小写16进制字符串</summary>
    public static string Encrypt(string plainText)
    {
        var hash = MD5.HashData(Encoding.UTF8.GetBytes(plainText));
        var sb = new StringBuilder(32);
        foreach (var b in hash)
            sb.Append(b.ToString("x2"));
        return sb.ToString();
    }
}
