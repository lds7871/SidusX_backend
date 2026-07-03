using SidusX.DTOs.Response;
using SidusX.Repositories;

namespace SidusX.Services;

public class AnnouncementService
{
    private readonly AnnouncementRepository _repo;
    public AnnouncementService(AnnouncementRepository repo) => _repo = repo;

    public async Task<AnnouncementResponse?> GetLatestAsync()
    {
        var a = await _repo.GetLatestAsync();
        return a == null ? null : new AnnouncementResponse { AnnId = a.AnnId, Content = a.Content, CreateTime = a.CreateTime };
    }

    public async Task<List<AnnouncementResponse>> GetAllAsync()
    {
        var list = await _repo.GetAllAsync();
        return list.Select(a => new AnnouncementResponse { AnnId = a.AnnId, Content = a.Content, CreateTime = a.CreateTime }).ToList();
    }

    public async Task<long> CreateAsync(string content)
    {
        if (string.IsNullOrWhiteSpace(content)) throw new ArgumentException("公告内容不能为空");
        return await _repo.InsertAsync(content);
    }
}
