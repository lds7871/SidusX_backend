package LDS.Person.service.impl;

import LDS.Person.entity.NasaDailyImage;
import LDS.Person.repository.NasaDailyImageMapper;
import LDS.Person.service.NasaDailyImageService;
import LDS.Person.util.DeepSeekApiClient;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

/**
 * NASA APOD 每日图片信息服务实现类
 */
@Service
public class NasaDailyImageServiceImpl extends ServiceImpl<NasaDailyImageMapper, NasaDailyImage> 
        implements NasaDailyImageService {

    private static final Logger logger = LoggerFactory.getLogger(NasaDailyImageServiceImpl.class);

    private static final String NASA_API_URL = "https://api.nasa.gov/planetary/apod?api_key=N9J3VuhsFS0OrM55nLOamM3NgR8VlFk73kz2zW1C";

    private final ObjectMapper objectMapper;
    private final DeepSeekApiClient deepSeekClient;

    public NasaDailyImageServiceImpl() {
        this.objectMapper = new ObjectMapper();
        this.deepSeekClient = initializeDeepSeekClient();
    }

    /**
     * 初始化DeepSeekClient，如果初始化失败则返回null
     */
    private DeepSeekApiClient initializeDeepSeekClient() {
        try {
            return new DeepSeekApiClient();
        } catch (Exception e) {
            logger.warn("DeepSeekApiClient 初始化失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void fetchAndSaveApodImage() {
        try {
            logger.info("开始获取NASA APOD图片信息");

            // 1. 从NASA API获取图片信息
            String responseBody = fetchFromNasaApi();
            NasaApodResponse apodResponse = objectMapper.readValue(responseBody, NasaApodResponse.class);

            // 2. 翻译 explanation 为中文
            String translatedExplanation = apodResponse.getExplanation();
            if (deepSeekClient != null && apodResponse.getExplanation() != null) {
                try {
                    translatedExplanation = deepSeekClient.chatWithDefault(
                            apodResponse.getExplanation(),
                            "将翻译为中文"
                    );
                    //logger.info("explanation 翻译成功");
                } catch (Exception e) {
                    logger.warn("explanation 翻译失败，使用原始内容: {}", e.getMessage());
                    translatedExplanation = apodResponse.getExplanation();
                }
            } else {
                translatedExplanation = apodResponse.getExplanation();
            }

            // 3. 构建NasaDailyImage对象
            NasaDailyImage nasaImage = new NasaDailyImage();
            nasaImage.setCopyright(apodResponse.getCopyright());
            nasaImage.setExplanation(translatedExplanation);
            nasaImage.setMediaType(apodResponse.getMedia_type());
            nasaImage.setTitle(apodResponse.getTitle());
            nasaImage.setUrl(apodResponse.getHdurl() != null ? apodResponse.getHdurl() : apodResponse.getUrl());
            nasaImage.setCreateTime(LocalDateTime.now());

            // 4. 保存到数据库
            this.save(nasaImage);
            logger.info("NASA APOD图片信息已保存到数据库, 标题: {}", nasaImage.getTitle());

        } catch (Exception e) {
            logger.error("获取并保存NASA APOD图片失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从NASA API获取响应数据
     */
    private String fetchFromNasaApi() throws Exception {
        HttpClient httpClient = LDS.Person.config.HttpClientFactory.getInstance();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(NASA_API_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("NASA API 请求失败，状态码: " + response.statusCode());
        }

        return response.body();
    }

    /**
     * NASA APOD API 响应数据类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class NasaApodResponse {
        private String copyright;
        private String explanation;
        @JsonProperty("hdurl")
        private String hdurl;
        private String url;
        @JsonProperty("media_type")
        private String media_type;
        private String title;
        private String date;
    }
}
