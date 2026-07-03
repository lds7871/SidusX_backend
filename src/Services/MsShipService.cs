using SidusX.DTOs.Response;
using SidusX.Repositories;

namespace SidusX.Services;

public class MsShipService
{
    private readonly MsShipRepository _repo;
    public MsShipService(MsShipRepository repo) => _repo = repo;

    public async Task<long> CreateAsync(string content) => await _repo.InsertAsync(content);

    public async Task<MsShipResponse?> GetByIdAsync(long id)
    {
        var m = await _repo.GetByIdAsync(id);
        return m == null ? null : new MsShipResponse { MsId = m.MsId, Content = m.Content };
    }

    public async Task<List<MsShipResponse>> GetAllAsync()
    {
        var list = await _repo.GetAllAsync();
        return list.Select(m => new MsShipResponse { MsId = m.MsId, Content = m.Content }).ToList();
    }

    public async Task<bool> UpdateAsync(long id, string content) => await _repo.UpdateAsync(id, content);
    public async Task<bool> DeleteAsync(long id) => await _repo.DeleteAsync(id);
}
