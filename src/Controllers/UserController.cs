using Microsoft.AspNetCore.Mvc;
using SidusX.Config;
using SidusX.DTOs.Request;
using SidusX.DTOs.Response;
using SidusX.Services;

namespace SidusX.Controllers;

[ApiController]
[Route("GHapi/user")]
public class UserController : ControllerBase
{
    private readonly UserService _service;
    public UserController(UserService service) => _service = service;

    [HttpPost("login")]
    [BypassIpWhitelist]
    public async Task<R<UserInfoResponse>> Login([FromBody] UserLoginRequest req)
    {
        try { return R<UserInfoResponse>.Ok(await _service.LoginAsync(req, HttpContext.Session)); }
        catch (ArgumentException e) { return R<UserInfoResponse>.Error(e.Message); }
    }

    [HttpPost("register/sendcode")]
    [BypassIpWhitelist]
    public async Task<R<string>> RegisterSendCode([FromBody] UserRegisterSendCodeRequest req)
    {
        try { await _service.SendRegisterCodeAsync(req); return R<string>.Ok("验证码已发送"); }
        catch (ArgumentException e) { return R<string>.Error(e.Message); }
    }

    [HttpPost("register/confirm")]
    [BypassIpWhitelist]
    public async Task<R<UserInfoResponse>> RegisterConfirm([FromBody] UserRegisterConfirmRequest req)
    {
        try { return R<UserInfoResponse>.Ok(await _service.ConfirmRegisterAsync(req)); }
        catch (ArgumentException e) { return R<UserInfoResponse>.Error(e.Message); }
    }

    [HttpPost("logout")]
    public async Task<R<string>> Logout() { await _service.LogoutAsync(HttpContext.Session); return R<string>.Ok("已登出"); }

    [HttpPost("password/sendcode")]
    [BypassIpWhitelist]
    public async Task<R<string>> PasswordSendCode([FromBody] ChangePasswordSendCodeRequest req)
    {
        try { await _service.SendChangePasswordCodeAsync(req); return R<string>.Ok("验证码已发送"); }
        catch (ArgumentException e) { return R<string>.Error(e.Message); }
    }

    [HttpPost("password/change")]
    [BypassIpWhitelist]
    public async Task<R<string>> PasswordChange([FromBody] ChangePasswordConfirmRequest req)
    {
        try { await _service.ChangePasswordAsync(req); return R<string>.Ok("密码修改成功"); }
        catch (ArgumentException e) { return R<string>.Error(e.Message); }
    }

    [HttpPost("cover/update")]
    public async Task<R<string>> UpdateCover([FromBody] UpdateCoverRequest req)
    {
        try { await _service.UpdateCoverAsync(req.UserId, req.Cover); return R<string>.Ok("头像更新成功"); }
        catch (ArgumentException e) { return R<string>.Error(e.Message); }
    }

    [HttpPost("place/update")]
    public async Task<R<string>> UpdatePlace([FromBody] UpdatePlaceRequest req)
    {
        try { await _service.UpdatePlaceAsync(req.UserId, req.Place); return R<string>.Ok("地区更新成功"); }
        catch (ArgumentException e) { return R<string>.Error(e.Message); }
    }

    [HttpPost("achievement/update")]
    public async Task<R<string>> UpdateAchievement([FromBody] UpdateGameAchievementRequest req)
    {
        try { await _service.UpdateGameAchievementAsync(req); return R<string>.Ok("成就更新成功"); }
        catch (ArgumentException e) { return R<string>.Error(e.Message); }
    }

    [HttpGet("achievement/{userId}")]
    public async Task<R<GameAchievementResponse>> GetAchievement(long userId)
    {
        try { return R<GameAchievementResponse>.Ok(await _service.GetGameAchievementAsync(userId)); }
        catch (ArgumentException e) { return R<GameAchievementResponse>.Error(e.Message); }
    }
}
