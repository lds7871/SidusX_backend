using System.Text.Json.Serialization;

namespace SidusX.DTOs.Response;

public class JsonResponse
{
    [JsonPropertyName("success")] public bool Success { get; set; }
    [JsonPropertyName("message")] public string? Message { get; set; }
    [JsonPropertyName("data")] public object? Data { get; set; }

    public static JsonResponse Ok(string message = "操作成功", object? data = null) =>
        new() { Success = true, Message = message, Data = data };
    public static JsonResponse Failure(string message) =>
        new() { Success = false, Message = message };
}

public class PageResponse<T>
{
    [JsonPropertyName("total")] public long Total { get; set; }
    [JsonPropertyName("page")] public int Page { get; set; }
    [JsonPropertyName("page_size")] public int PageSize { get; set; }
    [JsonPropertyName("data")] public List<T> Data { get; set; } = new();
}

/// <summary>通用泛型响应包装器</summary>
public class R<T>
{
    [System.Text.Json.Serialization.JsonPropertyName("code")] public int Code { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("msg")] public string? Msg { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("data")] public T? Data { get; set; }

    public static R<T> Ok(T data, string msg = "success") => new() { Code = 200, Msg = msg, Data = data };
    public static R<T> Error(string msg, int code = 400) => new() { Code = code, Msg = msg };
}

public class RuntimeInfoResponse
{
    [System.Text.Json.Serialization.JsonPropertyName("runtime_version")] public string RuntimeVersion { get; set; } = string.Empty;
    [System.Text.Json.Serialization.JsonPropertyName("os_description")] public string OsDescription { get; set; } = string.Empty;
    [System.Text.Json.Serialization.JsonPropertyName("processor_count")] public int ProcessorCount { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("working_set")] public long WorkingSet { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("gc_total_memory")] public long GcTotalMemory { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("thread_count")] public int ThreadCount { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("uptime")] public string Uptime { get; set; } = string.Empty;
    [System.Text.Json.Serialization.JsonPropertyName("start_time")] public string StartTime { get; set; } = string.Empty;
    [System.Text.Json.Serialization.JsonPropertyName("current_time")] public string CurrentTime { get; set; } = string.Empty;
}

public class NginxLogResponse
{
    [System.Text.Json.Serialization.JsonPropertyName("exists")] public bool Exists { get; set; }
    [System.Text.Json.Serialization.JsonPropertyName("log_path")] public string LogPath { get; set; } = string.Empty;
    [System.Text.Json.Serialization.JsonPropertyName("lines")] public List<string> Lines { get; set; } = new();
    [System.Text.Json.Serialization.JsonPropertyName("total_lines")] public int TotalLines { get; set; }
}
