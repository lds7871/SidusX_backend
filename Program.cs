using System.Net;
using Microsoft.AspNetCore.Builder;
using Microsoft.Extensions.DependencyInjection;
using Npgsql;
using SidusX.BackgroundTasks;
using SidusX.Config;
using SidusX.Repositories;
using SidusX.Services;
using SidusX.Utils;

var builder = WebApplication.CreateBuilder(args);

// ─── Configuration bindings ─────────────────────────────────────────────────
builder.Services.Configure<SecuritySettings>(builder.Configuration.GetSection("Security"));
builder.Services.Configure<ProxySettings>(builder.Configuration.GetSection("Proxy"));
builder.Services.Configure<MailSettings>(builder.Configuration.GetSection("Mail"));
builder.Services.Configure<WebSocketConsoleSettings>(builder.Configuration.GetSection("WebSocketConsole"));

// ─── Database ────────────────────────────────────────────────────────────────
var connStr = builder.Configuration.GetConnectionString("Default")
              ?? throw new InvalidOperationException("缺少数据库连接字符串 ConnectionStrings:Default");
var dataSource = NpgsqlDataSource.Create(connStr);
builder.Services.AddSingleton(dataSource);

// ─── HTTP Clients ─────────────────────────────────────────────────────────────
var proxySection = builder.Configuration.GetSection("Proxy");
var proxyHost = proxySection["Host"];
var proxyPortStr = proxySection["Port"];

builder.Services.AddHttpClient("deepseek", c =>
{
    c.Timeout = TimeSpan.FromSeconds(120);
});

builder.Services.AddHttpClient("nasa", c => { c.Timeout = TimeSpan.FromSeconds(30); });
builder.Services.AddHttpClient("spacex", c => { c.Timeout = TimeSpan.FromSeconds(30); });

if (!string.IsNullOrEmpty(proxyHost) && int.TryParse(proxyPortStr, out var proxyPort) && proxyPort > 0)
{
    var proxy = new WebProxy(proxyHost, proxyPort);
    builder.Services.AddHttpClient("proxied", c => { })
        .ConfigurePrimaryHttpMessageHandler(() => new HttpClientHandler { Proxy = proxy, UseProxy = true });
}

// ─── Repositories ────────────────────────────────────────────────────────────
builder.Services.AddScoped<UserRepository>();
builder.Services.AddScoped<WikiRepository>();
builder.Services.AddScoped<WikiHistoryRepository>();
builder.Services.AddScoped<WikiReviewRepository>();
builder.Services.AddScoped<WikiNewRepository>();
builder.Services.AddScoped<WikiCommentRepository>();
builder.Services.AddScoped<ArticleRepository>();
builder.Services.AddScoped<AnnouncementRepository>();
builder.Services.AddScoped<NasaDailyImageRepository>();
builder.Services.AddScoped<FalconStatsRepository>();
builder.Services.AddScoped<MsShipRepository>();
builder.Services.AddScoped<RecentLaunchRepository>();

// ─── Services ────────────────────────────────────────────────────────────────
builder.Services.AddSingleton<VerifyCodeStore>();
builder.Services.AddScoped<EmailService>();
builder.Services.AddScoped<DeepSeekApiClient>();
builder.Services.AddScoped<UserService>();
builder.Services.AddScoped<WikiService>();
builder.Services.AddScoped<WikiCommentService>();
builder.Services.AddScoped<WikiNewService>();
builder.Services.AddScoped<WikiReviewService>();
builder.Services.AddScoped<ArticleService>();
builder.Services.AddScoped<AnnouncementService>();
builder.Services.AddScoped<NasaDailyImageService>();
builder.Services.AddScoped<FalconStatsService>();
builder.Services.AddScoped<RecentLaunchService>();
builder.Services.AddScoped<MsShipService>();

// ─── Background Tasks ─────────────────────────────────────────────────────────
builder.Services.AddHostedService<NasaDailyImageTask>();
builder.Services.AddHostedService<FalconDailyTask>();
builder.Services.AddHostedService<RecentLaunchTask>();
builder.Services.AddHostedService<NginxCollectionTask>();

// ─── Session ─────────────────────────────────────────────────────────────────
builder.Services.AddDistributedMemoryCache();
builder.Services.AddSession(opts =>
{
    opts.IdleTimeout = TimeSpan.FromMinutes(30);
    opts.Cookie.HttpOnly = true;
    opts.Cookie.IsEssential = true;
    opts.Cookie.Name = "SIDUSX_SESSION";
});

// ─── Controllers + JSON ──────────────────────────────────────────────────────
builder.Services.AddControllers()
    .AddJsonOptions(opts =>
    {
        opts.JsonSerializerOptions.PropertyNameCaseInsensitive = true;
        opts.JsonSerializerOptions.DefaultIgnoreCondition =
            System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
    });

// ─── Swagger ─────────────────────────────────────────────────────────────────
var swaggerEnabled = builder.Configuration.GetValue<bool>("Swagger:Enabled");
if (swaggerEnabled)
{
    builder.Services.AddEndpointsApiExplorer();
    builder.Services.AddSwaggerGen();
}

// ─── WebSockets ───────────────────────────────────────────────────────────────
builder.Services.AddSingleton<WebSocketConsoleHandler>();

// ─── URL / Kestrel ────────────────────────────────────────────────────────────
builder.WebHost.UseUrls("http://0.0.0.0:8100");

// ════════════════════════════════════════════════════════════════════════════
var app = builder.Build();

// ─── Redirect System.Console output to WebSocket ────────────────────────────
var wsConsoleHandler = app.Services.GetRequiredService<WebSocketConsoleHandler>();
var wsConsoleWriter = new WebSocketConsoleWriter(Console.Out, wsConsoleHandler);
Console.SetOut(wsConsoleWriter);
Console.SetError(wsConsoleWriter);

// ─── Middleware pipeline ──────────────────────────────────────────────────────
app.UseMiddleware<SecurityMiddleware>();
app.UseMiddleware<IpWhitelistMiddleware>();
app.UseMiddleware<ApiLogMiddleware>();

app.UseSession();

// WebSocket for /ws/console
app.UseWebSockets(new WebSocketOptions { KeepAliveInterval = TimeSpan.FromSeconds(30) });
app.Use(async (context, next) =>
{
    if (context.Request.Path == "/ws/console" && context.WebSockets.IsWebSocketRequest)
    {
        var handler = context.RequestServices.GetRequiredService<WebSocketConsoleHandler>();
        var ws = await context.WebSockets.AcceptWebSocketAsync();
        await handler.HandleAsync(context, ws);
    }
    else
    {
        await next();
    }
});

if (swaggerEnabled)
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.MapControllers();

Console.WriteLine("SidusX Backend (.NET 10.0) 已启动，监听端口: 8100");
app.Run();
