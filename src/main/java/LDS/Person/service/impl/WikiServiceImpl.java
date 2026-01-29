package LDS.Person.service.impl;

import LDS.Person.entity.Wiki;
import LDS.Person.repository.WikiMapper;
import LDS.Person.service.WikiService;
import LDS.Person.dto.request.WikiCreateRequest;
import LDS.Person.dto.request.WikiPageQueryRequest;
import LDS.Person.dto.response.WikiResponse;
import LDS.Person.dto.response.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Wiki 业务逻辑实现类
 * 所有数据库查询操作都在此实现
 */
@Service
public class WikiServiceImpl implements WikiService {

    private static final Logger log = LoggerFactory.getLogger(WikiServiceImpl.class);

    private static final String DEFAULT_USER = "system";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final WikiMapper wikiMapper;
    private final JdbcTemplate jdbcTemplate;

    public WikiServiceImpl(WikiMapper wikiMapper, JdbcTemplate jdbcTemplate) {
        this.wikiMapper = wikiMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 新增 Wiki 记录
     * create_user 和 update_user 如果未提供则默认设置为 "system"
     */
    @Override
    @Transactional
    public WikiResponse createWiki(WikiCreateRequest request) {
        // 验证必填字段
        if (request.getKeyName() == null || request.getKeyName().trim().isEmpty()) {
            log.warn("Wiki 键名为空");
            throw new IllegalArgumentException("Wiki 键名不能为空");
        }

        if (request.getTexts() == null || request.getTexts().trim().isEmpty()) {
            log.warn("Wiki 内容为空");
            throw new IllegalArgumentException("Wiki 内容不能为空");
        }

        // 检查键名唯一性
        Wiki existingWiki = selectByKeyName(request.getKeyName());
        if (existingWiki != null) {
            log.warn("Wiki 键名已存在: {}", request.getKeyName());
            throw new IllegalArgumentException("Wiki 键名已存在: " + request.getKeyName());
        }

        // 创建 Wiki 实体
        Wiki wiki = new Wiki();
        wiki.setKeyName(request.getKeyName());
        wiki.setTexts(request.getTexts());
        wiki.setTags(request.getTags());
        wiki.setVersion(1.00);

        // 设置创建用户，如果未提供则默认为 "system"
        String createUser = (request.getCreateUser() != null && !request.getCreateUser().trim().isEmpty())
                ? request.getCreateUser().trim()
                : DEFAULT_USER;
        String updateUser = (request.getUpdateUser() != null && !request.getUpdateUser().trim().isEmpty())
                ? request.getUpdateUser().trim()
                : DEFAULT_USER;

        wiki.setCreateUser(createUser);
        wiki.setUpdateUser(updateUser);

        LocalDateTime now = LocalDateTime.now();
        wiki.setCreateTime(now);
        wiki.setUpdateTime(now);

        // 保存到数据库
        int result = wikiMapper.insertWiki(wiki);
        if (result <= 0) {
            log.error("Wiki 保存失败: {}", request.getKeyName());
            throw new RuntimeException("Wiki 保存失败");
        }

        log.info("Wiki 创建成功 - ID: {}, KeyName: {}, CreateUser: {}", wiki.getWikiId(), wiki.getKeyName(), createUser);

        return convertToResponse(wiki);
    }

    /**
     * 删除 Wiki 记录
     */
    @Override
    @Transactional
    public boolean deleteWiki(Long wikiId) {
        if (wikiId == null || wikiId <= 0) {
            log.warn("Wiki ID 无效: {}", wikiId);
            throw new IllegalArgumentException("Wiki ID 无效");
        }

        // 检查记录是否存在
        Wiki wiki = selectById(wikiId);
        if (wiki == null) {
            log.warn("Wiki 不存在: {}", wikiId);
            return false;
        }

        int result = wikiMapper.deleteWikiById(wikiId);
        if (result > 0) {
            log.info("Wiki 删除成功 - ID: {}, KeyName: {}", wikiId, wiki.getKeyName());
            return true;
        } else {
            log.error("Wiki 删除失败 - ID: {}", wikiId);
            return false;
        }
    }

    /**
     * 分页查询 Wiki 列表
     * 支持多条件过滤（key_name、tags、create_time 范围）
     */
    @Override
    public PageResponse<WikiResponse> pageQuery(WikiPageQueryRequest request) {
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

        // 计算偏移量
        int offset = (page - 1) * pageSize;

        // 查询总数
        long totalCount = countByCondition(request);

        // 查询分页数据
        List<Wiki> wikiList = selectPageList(request, offset);

        // 转换为响应对象（分页查询不包含 texts 字段）
        List<WikiResponse> responseList = new ArrayList<>();
        for (Wiki wiki : wikiList) {
            responseList.add(convertToResponseWithoutTexts(wiki));
        }

        // 计算总页数
        long totalPages = (totalCount + pageSize - 1) / pageSize;

        log.debug("Wiki 分页查询 - 页码: {}, 每页: {}, 总数: {}", page, pageSize, totalCount);

        return new PageResponse<>(page, pageSize, totalCount, totalPages, responseList);
    }

    /**
     * 将 Wiki 实体转换为响应对象
     */
    private WikiResponse convertToResponse(Wiki wiki) {
        WikiResponse response = new WikiResponse();
        response.setWikiId(wiki.getWikiId());
        response.setKeyName(wiki.getKeyName());
        response.setTexts(wiki.getTexts());
        response.setTags(wiki.getTags());
        response.setVersion(wiki.getVersion());
        response.setCreateTime(wiki.getCreateTime() != null ? wiki.getCreateTime().format(DATE_FORMATTER) : null);
        response.setCreateUser(wiki.getCreateUser());
        response.setUpdateTime(wiki.getUpdateTime() != null ? wiki.getUpdateTime().format(DATE_FORMATTER) : null);
        response.setUpdateUser(wiki.getUpdateUser());
        return response;
    }

    /**
     * 将 Wiki 实体转换为响应对象（不包含 texts 字段）
     * 用于分页查询等不需要完整内容的场景
     */
    private WikiResponse convertToResponseWithoutTexts(Wiki wiki) {
        WikiResponse response = new WikiResponse();
        response.setWikiId(wiki.getWikiId());
        response.setKeyName(wiki.getKeyName());
        // 不设置 texts 字段
        response.setTags(wiki.getTags());
        response.setVersion(wiki.getVersion());
        response.setCreateTime(wiki.getCreateTime() != null ? wiki.getCreateTime().format(DATE_FORMATTER) : null);
        response.setCreateUser(wiki.getCreateUser());
        response.setUpdateTime(wiki.getUpdateTime() != null ? wiki.getUpdateTime().format(DATE_FORMATTER) : null);
        response.setUpdateUser(wiki.getUpdateUser());
        return response;
    }

    // ==================== 数据库查询方法 ====================

    /**
     * 根据键名查询 Wiki 记录（用于唯一性检查）
     */
    private Wiki selectByKeyName(String keyName) {
        String sql = "SELECT wiki_id, key_name, texts, tags, version, " +
                "create_time, create_user, update_time, update_user " +
                "FROM wiki WHERE key_name = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new WikiRowMapper(), keyName);
        } catch (Exception e) {
            log.debug("查询键名对应的 Wiki 不存在: {}", keyName);
            return null;
        }
    }

    /**
     * 根据 Wiki ID 查询单条记录
     */
    private Wiki selectById(Long wikiId) {
        String sql = "SELECT wiki_id, key_name, texts, tags, version, " +
                "create_time, create_user, update_time, update_user " +
                "FROM wiki WHERE wiki_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new WikiRowMapper(), wikiId);
        } catch (Exception e) {
            log.debug("查询 Wiki ID 不存在: {}", wikiId);
            return null;
        }
    }

    /**
     * 分页查询 Wiki 列表
     * 支持多条件过滤：key_name、tags、create_time 范围
     */
    private List<Wiki> selectPageList(WikiPageQueryRequest request, int offset) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT wiki_id, key_name, texts, tags, version, ");
        sql.append("create_time, create_user, update_time, update_user ");
        sql.append("FROM wiki WHERE 1=1 ");

        // 键名模糊查询
        if (request.getKeyName() != null && !request.getKeyName().trim().isEmpty()) {
            sql.append("AND key_name ILIKE ? ");
            params.add("%" + request.getKeyName() + "%");
        }

        // 标签模糊查询
        if (request.getTags() != null && !request.getTags().trim().isEmpty()) {
            sql.append("AND tags::text ILIKE ? ");
            params.add("%" + request.getTags() + "%");
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
        sql.append("ORDER BY create_time DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(request.getPageSize());
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), params.toArray(new Object[0]), new WikiRowMapper());
    }

    /**
     * 检查 Wiki 键名是否已存在
     */
    @Override
    public boolean isKeyNameExists(String keyName) {
        if (keyName == null || keyName.trim().isEmpty()) {
            return false;
        }
        Wiki wiki = selectByKeyName(keyName.trim());
        return wiki != null;
    }

    /**
     * 查询符合条件的总记录数
     */
    private long countByCondition(WikiPageQueryRequest request) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT COUNT(*) FROM wiki WHERE 1=1 ");

        // 键名模糊查询
        if (request.getKeyName() != null && !request.getKeyName().trim().isEmpty()) {
            sql.append("AND key_name ILIKE ? ");
            params.add("%" + request.getKeyName() + "%");
        }

        // 标签模糊查询
        if (request.getTags() != null && !request.getTags().trim().isEmpty()) {
            sql.append("AND tags::text ILIKE ? ");
            params.add("%" + request.getTags() + "%");
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
     * Wiki 行映射器
     * 将 ResultSet 转换为 Wiki 对象
     */
    private static class WikiRowMapper implements org.springframework.jdbc.core.RowMapper<Wiki> {
        @Override
        public Wiki mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
            Wiki wiki = new Wiki();
            wiki.setWikiId(rs.getLong("wiki_id"));
            wiki.setKeyName(rs.getString("key_name"));
            wiki.setTexts(rs.getString("texts"));

            // 处理 PostgreSQL 数组类型
            java.sql.Array sqlArray = rs.getArray("tags");
            if (sqlArray != null) {
                wiki.setTags((String[]) sqlArray.getArray());
            }

            wiki.setVersion(rs.getDouble("version"));

            // 处理 Timestamp 转换为 LocalDateTime
            java.sql.Timestamp createTime = rs.getTimestamp("create_time");
            if (createTime != null) {
                wiki.setCreateTime(createTime.toLocalDateTime());
            }

            wiki.setCreateUser(rs.getString("create_user"));

            java.sql.Timestamp updateTime = rs.getTimestamp("update_time");
            if (updateTime != null) {
                wiki.setUpdateTime(updateTime.toLocalDateTime());
            }

            wiki.setUpdateUser(rs.getString("update_user"));

            return wiki;
        }
    }
}
