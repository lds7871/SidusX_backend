package LDS.Person.service.impl;

import LDS.Person.config.HttpClientFactory;
import LDS.Person.dto.response.RecentLaunchDataResponse;
import LDS.Person.entity.RecentLaunch;
import LDS.Person.repository.RecentLaunchMapper;
import LDS.Person.service.RecentLaunchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 最近发射数据服务实现类
 */
@Service
public class RecentLaunchServiceImpl implements RecentLaunchService {

  private static final Logger logger = LoggerFactory.getLogger(RecentLaunchServiceImpl.class);

  private static final String LAUNCH_API_URL = "https://ll.thespacedevs.com/2.3.0/launches/?limit=1&ordering=-last_updated&format=json";

  private final ObjectMapper objectMapper;
  private final RecentLaunchMapper recentLaunchMapper;

  public RecentLaunchServiceImpl(RecentLaunchMapper recentLaunchMapper) {
    this.recentLaunchMapper = recentLaunchMapper;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void fetchAndSaveRecentLaunch() {
    try {
      // 1. 从 API 获取数据
      logger.info("开始请求最近发射数据 API: {}", LAUNCH_API_URL);
      String responseBody = fetchFromApi();
      logger.info("API 响应成功，数据长度: {} 字符", responseBody.length());

      // 2. 解析 JSON，取 results[0] 作为核心数据
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode results = root.path("results");
      if (results.isEmpty()) {
        logger.warn("API 返回的 results 列表为空，跳过本次保存");
        return;
      }
      JsonNode firstResult = results.get(0);
      String launchName = firstResult.path("name").asText("unknown");
      String lastUpdated = firstResult.path("last_updated").asText("unknown");

      // 将完整响应 JSON 存入 data 字段（保留原始结构）
      String dataJson = responseBody;

      // 3. 构建实体并保存
      RecentLaunch recentLaunch = new RecentLaunch();
      recentLaunch.setData(dataJson);
      recentLaunch.setGetTime(LocalDateTime.now());

      int inserted = recentLaunchMapper.insert(recentLaunch);
      if (inserted > 0) {
        logger.info("发射数据已保存成功 | 任务名称: {} | last_updated: {} | 数据库 ID: {}",
            launchName, lastUpdated, recentLaunch.getId());
      } else {
        logger.warn("发射数据保存失败（insert 返回 0）");
        return;
      }

      // 4. 检查并清理超出 5 条的旧记录
      int totalCount = recentLaunchMapper.selectCount();
      logger.info("当前表内记录总数: {}", totalCount);
      if (totalCount > 5) {
        int deleted = recentLaunchMapper.deleteExceeding();
        logger.info("已清理超出限制的旧记录，删除 {} 条，当前保留最新 5 条", deleted);
      } else {
        logger.info("记录数未超过 5 条，无需清理");
      }

    } catch (Exception e) {
      logger.error("获取并保存最近发射数据失败: {}", e.getMessage(), e);
    }
  }

  /**
   * 向 Space Launch API 发送 GET 请求，返回响应体字符串
   */
  private String fetchFromApi() throws Exception {
    HttpClient httpClient = HttpClientFactory.getInstance();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(LAUNCH_API_URL))
        .GET()
        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        .header("Accept", "application/json")
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throw new Exception("API 请求失败，HTTP 状态码: " + response.statusCode());
    }

    return response.body();
  }

  @Override
  public RecentLaunchDataResponse getLatestData() {
    try {
      RecentLaunch latest = recentLaunchMapper.selectLatest();
      if (latest == null) {
        logger.warn("recent_launch 表内暂无数据");
        return null;
      }
      RecentLaunchDataResponse response = new RecentLaunchDataResponse();
      response.setId(latest.getId());
      response.setData(latest.getData());
      LocalDateTime getTime = latest.getGetTime();
      if (getTime != null) {
        response.setGetTime(getTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      }
      return response;
    } catch (Exception e) {
      logger.error("查询最新发射数据失败: {}", e.getMessage(), e);
      return null;
    }
  }
}
