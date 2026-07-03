using System.Text.Json;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Models;
using SidusX.Repositories;
using SidusX.Utils;

namespace SidusX.Services;

public class UserService
{
    private const int LoginExpireHours = 24;
    private readonly ILogger<UserService> _logger;
    private readonly UserRepository _userRepo;
    private readonly EmailService _emailService;
    private readonly VerifyCodeStore _verifyCodeStore;

    public UserService(ILogger<UserService> logger, UserRepository userRepo,
        EmailService emailService, VerifyCodeStore verifyCodeStore)
    {
        _logger = logger;
        _userRepo = userRepo;
        _emailService = emailService;
        _verifyCodeStore = verifyCodeStore;
    }

    public async Task<UserInfoResponse> LoginAsync(UserLoginRequest request, ISession session)
    {
        if (string.IsNullOrWhiteSpace(request.Password)) throw new ArgumentException("密码不能为空");

        User? user = null;
        if (!string.IsNullOrWhiteSpace(request.Mail))
            user = await _userRepo.SelectByMailAsync(request.Mail.Trim());
        else if (!string.IsNullOrWhiteSpace(request.Phone))
            user = await _userRepo.SelectByPhoneAsync(request.Phone.Trim());
        else
            throw new ArgumentException("邮箱或手机号不能为空");

        if (user == null) throw new ArgumentException("账号不存在");

        var inputHash = PasswordHelper.Encrypt(request.Password);
        if (inputHash != user.PasswordHash) throw new ArgumentException("密码错误");

        var expiredTime = DateTime.Now.AddHours(LoginExpireHours);
        await _userRepo.UpdateExpiredTimeAsync(user.UserId, expiredTime);
        user.ExpiredTime = expiredTime;

        session.SetString("LOGIN_USER_ID", user.UserId.ToString());
        _logger.LogInformation("用户登录成功 - userId: {UserId}, mail: {Mail}", user.UserId, user.Mail);
        return ToInfoResponse(user);
    }

    public async Task SendRegisterCodeAsync(UserRegisterSendCodeRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Mail)) throw new ArgumentException("邮箱不能为空");
        if (string.IsNullOrWhiteSpace(request.Name)) throw new ArgumentException("姓名不能为空");
        if (string.IsNullOrWhiteSpace(request.Password)) throw new ArgumentException("密码不能为空");

        var mail = request.Mail.Trim();
        if (await _userRepo.SelectByMailAsync(mail) != null) throw new ArgumentException("该邮箱已被注册");
        if (!string.IsNullOrWhiteSpace(request.Phone) && await _userRepo.SelectByPhoneAsync(request.Phone.Trim()) != null)
            throw new ArgumentException("该手机号已被注册");

        request.Password = PasswordHelper.Encrypt(request.Password);
        _verifyCodeStore.StorePendingRegister(mail, JsonSerializer.Serialize(request));

        var code = _verifyCodeStore.GenerateAndStore(mail);
        _emailService.SendSimpleMail(mail, "【账号注册验证码】", $"您正在注册账号，验证码为：{code}，有效期5分钟，请勿泄露。");
        _logger.LogInformation("注册验证码已发送 - mail: {Mail}", mail);
    }

    public async Task<UserInfoResponse> ConfirmRegisterAsync(UserRegisterConfirmRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Mail)) throw new ArgumentException("邮箱不能为空");
        if (string.IsNullOrWhiteSpace(request.VerifyCode)) throw new ArgumentException("验证码不能为空");

        var mail = request.Mail.Trim();
        if (!_verifyCodeStore.Verify(mail, request.VerifyCode)) throw new ArgumentException("验证码错误或已过期");

        var pendingJson = _verifyCodeStore.GetPendingRegister(mail);
        if (pendingJson == null) throw new ArgumentException("注册信息已失效，请重新发送验证码");

        var pending = JsonSerializer.Deserialize<UserRegisterSendCodeRequest>(pendingJson)
                      ?? throw new InvalidOperationException("注册信息解析失败");

        var user = new User
        {
            Name = pending.Name,
            Mail = mail,
            Phone = pending.Phone,
            PasswordHash = pending.Password,
            Place = pending.Place,
            AchievementJson = "{}"
        };
        user.UserId = await _userRepo.InsertAsync(user);

        _verifyCodeStore.Remove(mail);
        _verifyCodeStore.RemovePendingRegister(mail);

        _logger.LogInformation("用户注册成功 - userId: {UserId}, mail: {Mail}", user.UserId, mail);
        return ToInfoResponse(user);
    }

    public async Task LogoutAsync(ISession session)
    {
        var userIdStr = session.GetString("LOGIN_USER_ID");
        if (!string.IsNullOrEmpty(userIdStr) && long.TryParse(userIdStr, out var userId))
        {
            await _userRepo.UpdateExpiredTimeAsync(userId, DateTime.Now);
            _logger.LogInformation("用户登出 - userId: {UserId}", userId);
        }
        session.Clear();
    }

    public async Task SendChangePasswordCodeAsync(ChangePasswordSendCodeRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Mail)) throw new ArgumentException("邮箱不能为空");
        var mail = request.Mail.Trim();
        if (await _userRepo.SelectByMailAsync(mail) == null) throw new ArgumentException("该邮箱未注册");
        var code = _verifyCodeStore.GenerateAndStore(mail);
        _emailService.SendSimpleMail(mail, "【修改密码验证码】", $"您正在修改账号密码，验证码为：{code}，有效期5分钟，请勿泄露。");
    }

    public async Task ChangePasswordAsync(ChangePasswordConfirmRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Mail)) throw new ArgumentException("邮箱不能为空");
        if (string.IsNullOrWhiteSpace(request.VerifyCode)) throw new ArgumentException("验证码不能为空");
        if (string.IsNullOrWhiteSpace(request.NewPassword)) throw new ArgumentException("新密码不能为空");
        var mail = request.Mail.Trim();
        if (!_verifyCodeStore.Verify(mail, request.VerifyCode)) throw new ArgumentException("验证码错误或已过期");
        var user = await _userRepo.SelectByMailAsync(mail) ?? throw new ArgumentException("该邮箱未注册");
        await _userRepo.UpdatePasswordHashAsync(user.UserId, PasswordHelper.Encrypt(request.NewPassword));
        _verifyCodeStore.Remove(mail);
    }

    public async Task UpdateCoverAsync(long userId, string coverBase64)
    {
        if (userId <= 0) throw new ArgumentException("用户ID不能为空或无效");
        if (string.IsNullOrWhiteSpace(coverBase64)) throw new ArgumentException("头像不能为空");
        if (await _userRepo.SelectByIdAsync(userId) == null) throw new ArgumentException("用户不存在");
        await _userRepo.UpdateCoverAsync(userId, coverBase64);
    }

    public async Task UpdatePlaceAsync(long userId, string place)
    {
        if (userId <= 0) throw new ArgumentException("用户ID不能为空或无效");
        if (string.IsNullOrWhiteSpace(place)) throw new ArgumentException("地区不能为空");
        if (await _userRepo.SelectByIdAsync(userId) == null) throw new ArgumentException("用户不存在");
        await _userRepo.UpdatePlaceAsync(userId, place.Trim());
    }

    public async Task UpdateGameAchievementAsync(UpdateGameAchievementRequest request)
    {
        if (request.Userid <= 0) throw new ArgumentException("用户ID不能为空或无效");
        if (string.IsNullOrWhiteSpace(request.Gamename)) throw new ArgumentException("游戏名称不能为空");
        var user = await _userRepo.SelectByIdAsync(request.Userid) ?? throw new ArgumentException("用户不存在");
        var achievementJson = string.IsNullOrWhiteSpace(user.AchievementJson) ? "{}" : user.AchievementJson;
        var dict = JsonSerializer.Deserialize<Dictionary<string, JsonElement>>(achievementJson) ?? new();
        dict[request.Gamename] = JsonSerializer.SerializeToElement(request.Gamescore);
        await _userRepo.UpdateAchievementJsonAsync(request.Userid, JsonSerializer.Serialize(dict));
    }

    public async Task<GameAchievementResponse> GetGameAchievementAsync(long userId)
    {
        if (userId <= 0) throw new ArgumentException("用户ID不能为空或无效");
        var user = await _userRepo.SelectByIdAsync(userId) ?? throw new ArgumentException("用户不存在");
        var json = string.IsNullOrWhiteSpace(user.AchievementJson) ? "{}" : user.AchievementJson;
        return new GameAchievementResponse { UserId = userId, Achievements = JsonSerializer.Deserialize<object>(json) };
    }

    private static UserInfoResponse ToInfoResponse(User user) => new()
    {
        UserId = user.UserId,
        Name = user.Name,
        Cover = user.Cover,
        Phone = user.Phone,
        Mail = user.Mail,
        Place = user.Place,
        AchievementJson = user.AchievementJson,
        ExpiredTime = user.ExpiredTime
    };
}
