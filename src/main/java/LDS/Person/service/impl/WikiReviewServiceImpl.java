package LDS.Person.service.impl;

import LDS.Person.dto.request.WikiReviewCreateRequest;
import LDS.Person.dto.request.WikiReviewPageQueryRequest;
import LDS.Person.dto.request.WikiReviewUpdateRequest;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.dto.response.WikiReviewListResponse;
import LDS.Person.dto.response.WikiReviewResponse;
import LDS.Person.entity.Wiki;
import LDS.Person.entity.WikiHistory;
import LDS.Person.entity.WikiReview;
import LDS.Person.repository.WikiHistoryMapper;
import LDS.Person.repository.WikiReviewMapper;
import LDS.Person.service.WikiReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Wiki 审核服务实现类
 */
@Service
public class WikiReviewServiceImpl implements WikiReviewService {

    private static final Logger log = LoggerFactory.getLogger(WikiReviewServiceImpl.class);
    private static final String DEFAULT_USER = "system";

    private final WikiReviewMapper wikiReviewMapper;
    private final WikiHistoryMapper wikiHistoryMapper;
    private final JdbcTemplate jdbcTemplate;

    public WikiReviewServiceImpl(WikiReviewMapper wikiReviewMapper, WikiHistoryMapper wikiHistoryMapper,
            JdbcTemplate jdbcTemplate) {
        this.wikiReviewMapper = wikiReviewMapper;
        this.wikiHistoryMapper = wikiHistoryMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public WikiReviewResponse createReview(WikiReviewCreateRequest request) {
        if (request.getWikiId() == null) {
            throw new IllegalArgumentException("关联的 Wiki ID 不能为空");
        }
        if (request.getTexts() == null || request.getTexts().trim().isEmpty()) {
            throw new IllegalArgumentException("审核内容不能为空");
        }

        WikiReview review = new WikiReview();
        review.setWikiId(request.getWikiId());
        review.setTexts(request.getTexts());
        review.setTags(request.getTags());
        review.setVersion(request.getVersion() != null ? request.getVersion() : 1.00);
        review.setUpdateTime(LocalDateTime.now());
        review.setUpdateUser(request.getUpdateUser() != null ? request.getUpdateUser() : DEFAULT_USER);
        review.setWikiStates(0); // 默认待审核

        int result = wikiReviewMapper.insertReview(review);
        if (result <= 0) {
            throw new RuntimeException("创建审核记录失败");
        }

        return convertToResponse(review);
    }

    @Override
    public PageResponse<WikiReviewListResponse> pageQuery(WikiReviewPageQueryRequest request) {
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        if (pageSize > 100)
            pageSize = 100;

        int offset = (page - 1) * pageSize;

        long total = countByCondition(request);
        List<WikiReview> list = selectPageList(request, offset, pageSize);

        List<WikiReviewListResponse> responses = list.stream()
                .map(this::convertToListResponse)
                .collect(Collectors.toList());

        long totalPages = (total + pageSize - 1) / pageSize;
        return new PageResponse<WikiReviewListResponse>(page, pageSize, total, totalPages, responses);
    }

    /**
     * 分页查询审核记录列表
     */
    private List<WikiReview> selectPageList(WikiReviewPageQueryRequest request, int offset, int pageSize) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT wikireview_id, wiki_id, texts, tags, version, update_time, update_user, wiki_states ");
        sql.append("FROM wiki_review WHERE 1=1 ");

        buildCondition(sql, params, request);

        sql.append("ORDER BY update_time DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
            WikiReview r = new WikiReview();
            r.setWikireviewId(rs.getLong("wikireview_id"));
            r.setWikiId(rs.getLong("wiki_id"));
            r.setTexts(rs.getString("texts"));
            java.sql.Array arr = rs.getArray("tags");
            if (arr != null) {
                r.setTags((String[]) arr.getArray());
            }
            r.setVersion(rs.getDouble("version"));
            r.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
            r.setUpdateUser(rs.getString("update_user"));
            r.setWikiStates(rs.getInt("wiki_states"));
            return r;
        }, params.toArray(new Object[0]));
    }

    /**
     * 统计符合条件的审核记录总数
     */
    private long countByCondition(WikiReviewPageQueryRequest request) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append("SELECT COUNT(*) FROM wiki_review WHERE 1=1 ");

        buildCondition(sql, params, request);

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray(new Object[0]));
        return count != null ? count : 0;
    }

    /**
     * 构建查询条件
     */
    private void buildCondition(StringBuilder sql, List<Object> params, WikiReviewPageQueryRequest request) {
        // wiki_id 精确匹配
        if (request.getWikiId() != null) {
            sql.append("AND wiki_id = ? ");
            params.add(request.getWikiId());
        }

        // wiki_states 精确匹配
        if (request.getWikiStates() != null) {
            sql.append("AND wiki_states = ? ");
            params.add(request.getWikiStates());
        }

        // update_user 模糊匹配
        if (request.getUpdateUser() != null && !request.getUpdateUser().trim().isEmpty()) {
            sql.append("AND update_user ILIKE ? ");
            params.add("%" + request.getUpdateUser().trim() + "%");
        }
    }

    @Override
    @Transactional
    public boolean updateReviewStatus(WikiReviewUpdateRequest request) {
        if (request.getWikireviewId() == null) {
            throw new IllegalArgumentException("审核 ID 不能为空");
        }
        if (request.getWikiStates() == null || (request.getWikiStates() != 1 && request.getWikiStates() != 2)) {
            throw new IllegalArgumentException("无效的审核状态");
        }

        // 1. 查询审核记录详情
        WikiReview review = selectReviewById(request.getWikireviewId());
        if (review == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        String updateUser = review.getUpdateUser(); // 从审核记录中获取提交时的用户

        // 2. 更新审核表状态
        int updateResult = wikiReviewMapper.updateReviewStatus(request.getWikireviewId(), request.getWikiStates(), now,
                updateUser);
        if (updateResult <= 0) {
            return false;
        }

        // 3. 如果审核通过 (1)，覆盖 wiki 表数据
        if (request.getWikiStates() == 1) {
            log.info("审核通过，开始覆盖 Wiki 数据 - WikiID: {}", review.getWikiId());
            overwriteWikiData(review, now, updateUser);
        }

        return true;
    }

    /**
     * 覆盖 Wiki 表数据
     */
    private void overwriteWikiData(WikiReview review, LocalDateTime now, String user) {
        // 1. 先查询wiki表的原始数据进行备份
        Wiki originalWiki = queryWikiById(review.getWikiId());
        if (originalWiki == null) {
            throw new RuntimeException("要更新的 Wiki 记录不存在，Wiki ID: " + review.getWikiId());
        }

        // 2. 备份原始wiki数据到wiki_history表
        backupWikiHistory(originalWiki);

        // 3. 覆盖 Wiki 表数据
        String sql = "UPDATE wiki SET texts = ?, tags = ?, version = ?, update_time = ?, update_user = ? WHERE wiki_id = ?";

        // 处理 PostgreSQL 数组
        java.sql.Array tagsArray = null;
        try {
            if (review.getTags() != null) {
                tagsArray = jdbcTemplate.getDataSource().getConnection().createArrayOf("text", review.getTags());
            }
        } catch (Exception e) {
            log.error("创建 SQL 数组失败", e);
        }

        int result = jdbcTemplate.update(sql,
                review.getTexts(),
                tagsArray,
                review.getVersion(),
                now,
                user,
                review.getWikiId());

        if (result <= 0) {
            throw new RuntimeException("覆盖 Wiki 数据失败，Wiki ID 可能不存在");
        }

        log.info("Wiki 数据已成功覆盖并备份 - WikiID: {}", review.getWikiId());
    }

    /**
     * 根据 Wiki ID 查询 Wiki 完整信息
     */
    private Wiki queryWikiById(Long wikiId) {
        String sql = "SELECT wiki_id, key_name, texts, tags, version, create_time, create_user, update_time, update_user FROM wiki WHERE wiki_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Wiki w = new Wiki();
                w.setWikiId(rs.getLong("wiki_id"));
                w.setKeyName(rs.getString("key_name"));
                w.setTexts(rs.getString("texts"));
                java.sql.Array arr = rs.getArray("tags");
                if (arr != null) {
                    w.setTags((String[]) arr.getArray());
                }
                w.setVersion(rs.getDouble("version"));
                w.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
                w.setCreateUser(rs.getString("create_user"));
                w.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
                w.setUpdateUser(rs.getString("update_user"));
                return w;
            }, wikiId);
        } catch (Exception e) {
            log.error("查询 Wiki 数据失败，Wiki ID: {}", wikiId, e);
            return null;
        }
    }

    /**
     * 备份 Wiki 数据到历史表
     */
    private void backupWikiHistory(Wiki wiki) {
        WikiHistory history = new WikiHistory(
                wiki.getWikiId(),
                wiki.getKeyName(),
                wiki.getTexts(),
                wiki.getTags(),
                wiki.getVersion(),
                wiki.getCreateTime(),
                wiki.getCreateUser(),
                wiki.getUpdateTime(),
                wiki.getUpdateUser());

        int result = wikiHistoryMapper.insertWikiHistory(history);
        if (result <= 0) {
            throw new RuntimeException("备份 Wiki 历史数据失败，Wiki ID: " + wiki.getWikiId());
        }

        log.info("Wiki 原始数据已备份到历史表 - WikiID: {}, HistoryID: {}", wiki.getWikiId(), history.getHistoryId());
    }

    /**
     * 根据 ID 查询审核记录
     */
    private WikiReview selectReviewById(Long id) {
        String sql = "SELECT wikireview_id, wiki_id, texts, tags, version, update_time, update_user, wiki_states FROM wiki_review WHERE wikireview_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                WikiReview r = new WikiReview();
                r.setWikireviewId(rs.getLong("wikireview_id"));
                r.setWikiId(rs.getLong("wiki_id"));
                r.setTexts(rs.getString("texts"));
                java.sql.Array arr = rs.getArray("tags");
                if (arr != null) {
                    r.setTags((String[]) arr.getArray());
                }
                r.setVersion(rs.getDouble("version"));
                r.setUpdateTime(rs.getTimestamp("update_time").toLocalDateTime());
                r.setUpdateUser(rs.getString("update_user"));
                r.setWikiStates(rs.getInt("wiki_states"));
                return r;
            }, id);
        } catch (Exception e) {
            return null;
        }
    }

    private WikiReviewResponse convertToResponse(WikiReview review) {
        WikiReviewResponse response = new WikiReviewResponse();
        response.setWikireviewId(review.getWikireviewId());
        response.setWikiId(review.getWikiId());
        response.setTexts(review.getTexts());
        response.setTags(review.getTags());
        response.setVersion(review.getVersion());
        response.setUpdateTime(review.getUpdateTime());
        response.setUpdateUser(review.getUpdateUser());
        response.setWikiStates(review.getWikiStates());
        // 通过 wiki_id 查询对应的 key_name
        Wiki wiki = queryWikiById(review.getWikiId());
        if (wiki != null) {
            response.setKeyName(wiki.getKeyName());
        }
        return response;
    }

    private WikiReviewListResponse convertToListResponse(WikiReview review) {
        WikiReviewListResponse response = new WikiReviewListResponse();
        response.setWikireviewId(review.getWikireviewId());
        response.setWikiId(review.getWikiId());
        response.setTags(review.getTags());
        response.setUpdateTime(review.getUpdateTime());
        response.setWikiStates(review.getWikiStates());
        // 通过 wiki_id 查询对应的 key_name
        Wiki wiki = queryWikiById(review.getWikiId());
        if (wiki != null) {
            response.setKeyName(wiki.getKeyName());
        }
        return response;
    }

    @Override
    public WikiReviewResponse getReviewDetail(Long wikireviewId) {
        if (wikireviewId == null) {
            throw new IllegalArgumentException("审核 ID 不能为空");
        }
        WikiReview review = selectReviewById(wikireviewId);
        if (review == null) {
            throw new IllegalArgumentException("审核记录不存在");
        }
        return convertToResponse(review);
    }
}
