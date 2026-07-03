using System.Net.WebSockets;
using System.Text;
using System.Collections.Concurrent;
using Microsoft.Extensions.Options;

namespace SidusX.Config;

/// <summary>
/// WebSocket控制台处理器 - 将控制台输出广播到所有WebSocket客户端
/// </summary>
public class WebSocketConsoleHandler
{
    private readonly ILogger<WebSocketConsoleHandler> _logger;
    private readonly WebSocketConsoleSettings _settings;
    private readonly ConcurrentDictionary<string, WebSocket> _sessions = new();
    private readonly BlockingCollection<string> _messageQueue;
    private readonly List<string> _historyBuffer = new();
    private readonly object _historyLock = new();
    private static WebSocketConsoleHandler? _instance;

    public WebSocketConsoleHandler(ILogger<WebSocketConsoleHandler> logger, IOptions<WebSocketConsoleSettings> settings)
    {
        _logger = logger;
        _settings = settings.Value;
        _messageQueue = new BlockingCollection<string>(_settings.MessageQueueSize);
        _instance = this;

        // 启动消息发送线程
        var senderThread = new Thread(ProcessMessageQueue) { IsBackground = true, Name = "WS-Console-Sender" };
        senderThread.Start();

        _logger.LogInformation("WebSocket控制台处理器已初始化");
    }

    public static WebSocketConsoleHandler? Instance => _instance;

    public async Task HandleAsync(HttpContext context, WebSocket webSocket)
    {
        var id = Guid.NewGuid().ToString("N");
        _sessions[id] = webSocket;
        _logger.LogInformation("WebSocket连接建立: {Id} (总连接数: {Count})", id, _sessions.Count);

        // 发送欢迎消息和历史消息
        await SendToSocket(webSocket, $"=== 正在使用控制台WebSocket ===\n连接ID: {id}\n当前时间: {DateTime.Now}\n================================\n\n");

        lock (_historyLock)
        {
            if (_historyBuffer.Count > 0)
            {
                var hist = "=== 最近的日志记录 ===\n" + string.Concat(_historyBuffer) + "=== 开始实时日志 ===\n\n";
                _ = SendToSocket(webSocket, hist);
            }
        }

        var buffer = new byte[4096];
        try
        {
            while (webSocket.State == WebSocketState.Open)
            {
                var result = await webSocket.ReceiveAsync(new ArraySegment<byte>(buffer), CancellationToken.None);
                if (result.MessageType == WebSocketMessageType.Close)
                    break;
            }
        }
        catch { /* connection closed */ }
        finally
        {
            _sessions.TryRemove(id, out _);
            _logger.LogInformation("WebSocket连接关闭: {Id} (总连接数: {Count})", id, _sessions.Count);
            if (webSocket.State != WebSocketState.Closed)
                await webSocket.CloseAsync(WebSocketCloseStatus.NormalClosure, "Closing", CancellationToken.None);
        }
    }

    public void Broadcast(string message)
    {
        if (string.IsNullOrEmpty(message)) return;

        lock (_historyLock)
        {
            _historyBuffer.Add(message);
            while (_historyBuffer.Count > _settings.HistoryBufferSize)
                _historyBuffer.RemoveAt(0);
        }

        if (_sessions.IsEmpty) return;
        if (!_messageQueue.TryAdd(message))
            _logger.LogWarning("消息队列已满，丢弃消息");
    }

    private void ProcessMessageQueue()
    {
        foreach (var message in _messageQueue.GetConsumingEnumerable())
        {
            try
            {
                foreach (var (id, socket) in _sessions)
                {
                    _ = SendToSocket(socket, message).ContinueWith(t =>
                    {
                        if (t.IsFaulted)
                        {
                            _sessions.TryRemove(id, out _);
                        }
                    });
                }
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "处理消息队列时发生错误");
            }
        }
    }

    private static async Task SendToSocket(WebSocket socket, string message)
    {
        if (socket.State != WebSocketState.Open) return;
        var bytes = Encoding.UTF8.GetBytes(message);
        await socket.SendAsync(new ArraySegment<byte>(bytes), WebSocketMessageType.Text, true, CancellationToken.None);
    }

    public int ActiveConnectionCount => _sessions.Count;
}

/// <summary>
/// 控制台输出重定向到WebSocket的自定义TextWriter
/// </summary>
public class WebSocketConsoleWriter : TextWriter
{
    private readonly TextWriter _original;
    private readonly WebSocketConsoleHandler _handler;
    private readonly StringBuilder _lineBuffer = new();

    public override Encoding Encoding => Encoding.UTF8;

    public WebSocketConsoleWriter(TextWriter original, WebSocketConsoleHandler handler)
    {
        _original = original;
        _handler = handler;
    }

    public override void Write(char value)
    {
        _original.Write(value);
        _lineBuffer.Append(value);
        if (value == '\n')
        {
            _handler.Broadcast(_lineBuffer.ToString());
            _lineBuffer.Clear();
        }
    }

    public override void Write(string? value)
    {
        if (value == null) return;
        _original.Write(value);
        _lineBuffer.Append(value);
        FlushBufferIfNewline();
    }

    public override void WriteLine(string? value)
    {
        _original.WriteLine(value);
        _handler.Broadcast((value ?? string.Empty) + "\n");
    }

    public override void Flush()
    {
        _original.Flush();
        if (_lineBuffer.Length > 0)
        {
            _handler.Broadcast(_lineBuffer.ToString());
            _lineBuffer.Clear();
        }
    }

    private void FlushBufferIfNewline()
    {
        var s = _lineBuffer.ToString();
        var idx = s.LastIndexOf('\n');
        if (idx >= 0)
        {
            _handler.Broadcast(s[..(idx + 1)]);
            _lineBuffer.Clear();
            _lineBuffer.Append(s[(idx + 1)..]);
        }
    }
}
