package LDS.Person.service.impl;

import LDS.Person.dto.request.NasaDailyImagePageQueryRequest;
import LDS.Person.dto.response.NasaDailyImageListResponse;
import LDS.Person.dto.response.NasaDailyImageDetailResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.entity.NasaDailyImage;
import LDS.Person.repository.NasaDailyImageMapper;
import LDS.Person.service.NasaDailyImageService;
import LDS.Person.util.DeepSeekApiClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * NASA APOD 每日图片信息服务实现类
 */
@Service
public class NasaDailyImageServiceImpl implements NasaDailyImageService {

    private static final Logger logger = LoggerFactory.getLogger(NasaDailyImageServiceImpl.class);

    private static final String NASA_API_URL = "https://api.nasa.gov/planetary/apod?api_key=N9J3VuhsFS0OrM55nLOamM3NgR8VlFk73kz2zW1C";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ObjectMapper objectMapper;
    private final DeepSeekApiClient deepSeekClient;
    private final NasaDailyImageMapper nasaDailyImageMapper;
    private final JdbcTemplate jdbcTemplate;

    public NasaDailyImageServiceImpl(NasaDailyImageMapper nasaDailyImageMapper, JdbcTemplate jdbcTemplate) {
        this.nasaDailyImageMapper = nasaDailyImageMapper;
        this.jdbcTemplate = jdbcTemplate;
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
                            "将翻译为中文");
                    // logger.info("explanation 翻译成功");
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

    @Override
    public boolean save(NasaDailyImage nasaDailyImage) {
        return nasaDailyImageMapper.insert(nasaDailyImage) > 0;
    }

    @Override
    public NasaDailyImage getById(Long apodId) {
        return nasaDailyImageMapper.selectById(apodId);
    }

    @Override
    public List<NasaDailyImage> list() {
        return nasaDailyImageMapper.selectAll();
    }

    /**
     * 分页查询NASA图片列表
     */
    @Override
    public PageResponse<NasaDailyImageListResponse> pageQuery(NasaDailyImagePageQueryRequest request) {
        // 验证分页参数
        Integer page = request.getPage();
        Integer pageSize = request.getPageSize();

        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 10) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大页面大小
        }

        int offset = (page - 1) * pageSize;

        // 查询总数
        long totalCount = countByCondition(request);

        // 查询分页数据
        List<NasaDailyImageListResponse> responseList = selectPageList(request, offset, pageSize);

        // 计算总页数
        long totalPages = (totalCount + pageSize - 1) / pageSize;

        logger.debug("NASA图片分页查询 - 页码: {}, 每页: {}, 总数: {}", page, pageSize, totalCount);

        return new PageResponse<>(page, pageSize, totalCount, totalPages, responseList);
    }

    /**
     * 获取NASA图片详细信息
     */
    @Override
    public NasaDailyImageDetailResponse getDetail(Long apodId) {
        if (apodId == null || apodId <= 0) {
            logger.warn("图片ID无效: {}", apodId);
            return null;
        }

        String sql = "SELECT apod_id, copyright, explanation, media_type, title, url, create_time " +
                "FROM nasa_daily_image WHERE apod_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new NasaDailyImageDetailRowMapper(), apodId);
        } catch (Exception e) {
            logger.debug("查询图片详情不存在: {}", apodId);
            return null;
        }
    }

    /**
     * 删除NASA图片记录
     */
    @Override
    @Transactional
    public boolean deleteById(Long apodId) {
        if (apodId == null || apodId <= 0) {
            logger.warn("图片ID无效: {}", apodId);
            return false;
        }

        int result = nasaDailyImageMapper.deleteById(apodId);
        if (result > 0) {
            logger.info("NASA图片删除成功 - ID: {}", apodId);
            return true;
        } else {
            logger.warn("NASA图片删除失败 - ID不存在或删除异常: {}", apodId);
            return false;
        }
    }

    /**
     * 分页查询NASA图片列表
     * 支持按标题和时间范围筛选
     */
    private List<NasaDailyImageListResponse> selectPageList(NasaDailyImagePageQueryRequest request, int offset,
            int pageSize) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT apod_id, title, create_time FROM nasa_daily_image WHERE 1=1 ");

        // 标题模糊查询
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            sql.append("AND title ILIKE ? ");
            params.add("%" + request.getTitle() + "%");
        }

        // 创建时间范围查询 - 开始时间
        if (request.getCreateTimeStart() != null && !request.getCreateTimeStart().trim().isEmpty()) {
            sql.append("AND create_time >= ?::timestamp ");
            params.add(request.getCreateTimeStart());
        }

        // 创建时间范围查询 - 结束时间
        if (request.getCreateTimeEnd() != null && !request.getCreateTimeEnd().trim().isEmpty()) {
            sql.append("AND create_time <= ?::timestamp ");
            params.add(request.getCreateTimeEnd());
        }

        // 排序和分页
        sql.append("ORDER BY create_time DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), params.toArray(new Object[0]), new NasaDailyImageListRowMapper());
    }

    /**
     * 查询符合条件的总记录数
     */
    private long countByCondition(NasaDailyImagePageQueryRequest request) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT COUNT(*) FROM nasa_daily_image WHERE 1=1 ");

        // 标题模糊查询
        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            sql.append("AND title ILIKE ? ");
            params.add("%" + request.getTitle() + "%");
        }

        // 创建时间范围查询 - 开始时间
        if (request.getCreateTimeStart() != null && !request.getCreateTimeStart().trim().isEmpty()) {
            sql.append("AND create_time >= ?::timestamp ");
            params.add(request.getCreateTimeStart());
        }

        // 创建时间范围查询 - 结束时间
        if (request.getCreateTimeEnd() != null && !request.getCreateTimeEnd().trim().isEmpty()) {
            sql.append("AND create_time <= ?::timestamp ");
            params.add(request.getCreateTimeEnd());
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(new Object[0]), Long.class);
        return count != null ? count : 0;
    }

    /**
     * NASA 图片列表行映射器
     */
    private static class NasaDailyImageListRowMapper
            implements org.springframework.jdbc.core.RowMapper<NasaDailyImageListResponse> {
        @Override
        public NasaDailyImageListResponse mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            NasaDailyImageListResponse response = new NasaDailyImageListResponse();
            response.setApodId(rs.getLong("apod_id"));
            response.setTitle(rs.getString("title"));

            java.sql.Timestamp createTime = rs.getTimestamp("create_time");
            if (createTime != null) {
                response.setCreateTime(createTime.toLocalDateTime().format(DATE_FORMATTER));
            }

            return response;
        }
    }

    /**
     * NASA 图片详细信息行映射器
     */
    private static class NasaDailyImageDetailRowMapper
            implements org.springframework.jdbc.core.RowMapper<NasaDailyImageDetailResponse> {
        @Override
        public NasaDailyImageDetailResponse mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            NasaDailyImageDetailResponse response = new NasaDailyImageDetailResponse();
            response.setApodId(rs.getLong("apod_id"));
            response.setCopyright(rs.getString("copyright"));
            response.setExplanation(rs.getString("explanation"));
            response.setMediaType(rs.getString("media_type"));
            response.setTitle(rs.getString("title"));
            response.setUrl(rs.getString("url"));

            java.sql.Timestamp createTime = rs.getTimestamp("create_time");
            if (createTime != null) {
                response.setCreateTime(createTime.toLocalDateTime().format(DATE_FORMATTER));
            }

            return response;
        }
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
