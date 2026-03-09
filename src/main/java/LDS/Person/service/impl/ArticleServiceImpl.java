package LDS.Person.service.impl;

import LDS.Person.entity.Article;
import LDS.Person.dto.request.ArticleQueryRequest;
import LDS.Person.dto.response.ArticleResponse;
import LDS.Person.dto.response.ArticleListResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.repository.ArticleMapper;
import LDS.Person.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 文章服务实现类
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ArticleServiceImpl.class);

    private final JdbcTemplate jdbcTemplate;
    private final ArticleMapper articleMapper;

    public ArticleServiceImpl(JdbcTemplate jdbcTemplate, ArticleMapper articleMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.articleMapper = articleMapper;
    }

    @Override
    public PageResponse<ArticleResponse> queryArticleByPage(Integer pageNum, Integer pageSize, String title,
            String tags) {
        try {
            // 设置默认值
            if (pageNum == null || pageNum < 1) {
                pageNum = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 10;
            }

            // 计算偏移量
            int offset = (pageNum - 1) * pageSize;

            // 构建查询条件
            StringBuilder sql = new StringBuilder();
            List<Object> params = new ArrayList<>();

            sql.append("SELECT article_id, title, cover, info, texts, tags, create_time, update_time ");
            sql.append("FROM article WHERE 1=1 ");

            // 按标题模糊查询
            if (title != null && !title.isEmpty()) {
                sql.append("AND title LIKE ? ");
                params.add("%" + title + "%");
            }

            // 按标签模糊查询
            if (tags != null && !tags.isEmpty()) {
                sql.append("AND tags LIKE ? ");
                params.add("%" + tags + "%");
            }

            // 统计总数（使用相同的查询条件）
            String countSql = sql.toString().replace(
                    "SELECT article_id, title, cover, info, texts, tags, create_time, update_time FROM article",
                    "SELECT COUNT(*) FROM article");
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray(new Object[0]));
            long totalCount = total != null ? total : 0;

            // 按更新时间倒序排列
            sql.append("ORDER BY update_time DESC ");

            // 分页查询
            sql.append("LIMIT ? OFFSET ?");
            params.add(pageSize);
            params.add(offset);

            logger.info("✅ 开始分页查询文章，pageNum: {}, pageSize: {}, title: {}, tags: {}", pageNum, pageSize, title, tags);
            List<ArticleResponse> records = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
                ArticleResponse response = new ArticleResponse();
                response.setArticleId(rs.getLong("article_id"));
                response.setTitle(rs.getString("title"));
                response.setCover(rs.getString("cover"));
                response.setInfo(rs.getString("info"));
                response.setTexts(rs.getString("texts"));
                response.setTags(rs.getString("tags"));

                java.sql.Timestamp createTime = rs.getTimestamp("create_time");
                if (createTime != null) {
                    response.setCreateTime(createTime.toLocalDateTime());
                }
                java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
                if (updateTime != null) {
                    response.setUpdateTime(updateTime.toLocalDateTime());
                }
                return response;
            }, params.toArray(new Object[0]));

            logger.info("✅ 分页查询成功，查询到 {} 条记录", totalCount);

            long totalPages = (totalCount + pageSize - 1) / pageSize;
            return new PageResponse<>(pageNum, pageSize, totalCount, totalPages, records);
        } catch (Exception e) {
            logger.error("❌ 分页查询文章失败", e);
            throw new RuntimeException("查询失败: " + e.getMessage());
        }
    }

    @Override
    public PageResponse<ArticleListResponse> pageQuery(ArticleQueryRequest request) {
        int page = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        if (pageSize > 100)
            pageSize = 100;

        int offset = (page - 1) * pageSize;

        long total = countByCondition(request);
        List<ArticleListResponse> list = selectPageList(request, offset, pageSize);

        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageResponse<ArticleListResponse>(page, pageSize, total, totalPages, list);
    }

    /**
     * 分页查询文章列表
     */
    private List<ArticleListResponse> selectPageList(ArticleQueryRequest request, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();

        sql.append("SELECT article_id, title, cover, info, tags ");
        sql.append("FROM article WHERE 1=1 ");

        buildCondition(sql, params, request);

        sql.append("ORDER BY update_time DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        logger.info("✅ 分页查询文章 SQL: {}", sql);
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            ArticleListResponse response = new ArticleListResponse();
            response.setArticleId(rs.getLong("article_id"));
            response.setTitle(rs.getString("title"));
            response.setCover(rs.getString("cover"));
            response.setInfo(rs.getString("info"));
            response.setTags(rs.getString("tags"));
            return response;
        }, params.toArray(new Object[0]));
    }

    /**
     * 统计符合条件的文章总数
     */
    private long countByCondition(ArticleQueryRequest request) {
        StringBuilder sql = new StringBuilder();
        ArrayList<Object> params = new ArrayList<>();

        sql.append("SELECT COUNT(*) FROM article WHERE 1=1 ");

        buildCondition(sql, params, request);

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray(new Object[0]));
        return count != null ? count : 0;
    }

    /**
     * 构建查询条件
     */
    private void buildCondition(StringBuilder sql, ArrayList<Object> params, ArticleQueryRequest request) {
        // title 模糊匹配
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            sql.append("AND title LIKE ? ");
            params.add("%" + request.getTitle() + "%");
        }

        // tags 模糊匹配
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            sql.append("AND tags::text LIKE ? ");
            params.add("%" + request.getTags() + "%");
        }
    }

    @Override
    public boolean createArticle(Article article) {
        try {
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            article.setCreateTime(now);
            article.setUpdateTime(now);

            logger.info("✅ 开始创建文章，标题: {}", article.getTitle());
            int result = articleMapper.insert(article);
            logger.info("✅ 文章创建成功，文章ID: {}", article.getArticleId());

            return result > 0;
        } catch (Exception e) {
            logger.error("❌ 创建文章失败", e);
            throw new RuntimeException("创建失败: " + e.getMessage());
        }
    }

    @Override
    public Article getById(Long articleId) {
        return articleMapper.selectById(articleId);
    }

    @Override
    public boolean updateById(Article article) {
        article.setUpdateTime(LocalDateTime.now());
        return articleMapper.updateById(article) > 0;
    }

    @Override
    public boolean removeById(Long articleId) {
        return articleMapper.deleteById(articleId) > 0;
    }

    @Override
    public List<Article> list() {
        return articleMapper.selectAll();
    }

    @Override
    public Article getLatestArticle() {
        logger.info("✅ 查询最新文章");
        return articleMapper.selectLatestArticle();
    }
}
