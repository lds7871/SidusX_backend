package LDS.Person.service.impl;

import LDS.Person.config.HttpClientFactory;
import LDS.Person.entity.FalconStats;
import LDS.Person.repository.FalconStatsMapper;
import LDS.Person.service.FalconStatsService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * SpaceX Falcon 火箭统计数据服务实现类
 */
@Service
public class FalconStatsServiceImpl implements FalconStatsService {

    private static final Logger logger = LoggerFactory.getLogger(FalconStatsServiceImpl.class);

    private static final String FALCON_API_URL = "https://content.spacex.com/api/spacex-website/launches-page-stats";

    private final ObjectMapper objectMapper;
    private final FalconStatsMapper falconStatsMapper;

    public FalconStatsServiceImpl(FalconStatsMapper falconStatsMapper) {
        this.falconStatsMapper = falconStatsMapper;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void fetchAndSaveFalconStats() {
        try {
            logger.info("开始获取SpaceX Falcon火箭统计数据");

            // 1. 从API获取数据
            String responseBody = fetchFromFalconApi();
            FalconApiResponse apiResponse = objectMapper.readValue(responseBody, FalconApiResponse.class);

            // 2. 构建FalconStats对象
            FalconStats falconStats = new FalconStats();
            falconStats.setDocumentId(apiResponse.getDocumentId());
            falconStats.setTotalLaunches(apiResponse.getTotalLaunches());
            falconStats.setTotalLandings(apiResponse.getTotalLandings());
            falconStats.setTotalReflights(apiResponse.getTotalReflights());
            falconStats.setCreatedAt(LocalDateTime.now());

            // 3. 保存到数据库
            this.save(falconStats);
            logger.info("SpaceX Falcon统计数据已保存到数据库, 总发射次数: {}, 总着陆次数: {}, 总复用次数: {}",
                    falconStats.getTotalLaunches(), falconStats.getTotalLandings(), falconStats.getTotalReflights());

        } catch (Exception e) {
            logger.error("获取并保存SpaceX Falcon统计数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从SpaceX API获取响应数据
     */
    private String fetchFromFalconApi() throws Exception {
        HttpClient httpClient = HttpClientFactory.getInstance();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(FALCON_API_URL))
                .GET()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Failed to fetch Falcon stats, status code: " + response.statusCode());
        }

        return response.body();
    }

    @Override
    public boolean save(FalconStats falconStats) {
        try {
            int result = falconStatsMapper.insert(falconStats);
            return result > 0;
        } catch (Exception e) {
            logger.error("保存Falcon统计数据失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public FalconStats getById(Long falconId) {
        try {
            return falconStatsMapper.selectById(falconId);
        } catch (Exception e) {
            logger.error("查询Falcon统计数据失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<FalconStats> list() {
        try {
            return falconStatsMapper.selectAll();
        } catch (Exception e) {
            logger.error("查询Falcon统计数据列表失败: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * SpaceX API 响应对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FalconApiResponse {
        @JsonProperty("documentId")
        private String documentId;

        @JsonProperty("totalLaunches")
        private Integer totalLaunches;

        @JsonProperty("totalLandings")
        private Integer totalLandings;

        @JsonProperty("totalReflights")
        private Integer totalReflights;
    }
}
