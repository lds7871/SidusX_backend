package LDS.Person.service.impl;

import LDS.Person.dto.request.WikiReviewCreateRequest;
import LDS.Person.dto.request.WikiReviewUpdateRequest;
import LDS.Person.dto.response.WikiReviewResponse;
import LDS.Person.entity.WikiReview;
import LDS.Person.repository.WikiReviewMapper;
import LDS.Person.service.WikiReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Wiki 审核服务实现类
 */
@Service
public class WikiReviewServiceImpl implements WikiReviewService {

    private static final Logger log = LoggerFactory.getLogger(WikiReviewServiceImpl.class);
    private static final String DEFAULT_USER = "system";

    private final WikiReviewMapper wikiReviewMapper;
    private final JdbcTemplate jdbcTemplate;

    public WikiReviewServiceImpl(WikiReviewMapper wikiReviewMapper, JdbcTemplate jdbcTemplate) {
        this.wikiReviewMapper = wikiReviewMapper;
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
        int updateResult = wikiReviewMapper.updateReviewStatus(request.getWikireviewId(), request.getWikiStates(), now, updateUser);
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
            review.getWikiId()
        );

        if (result <= 0) {
            throw new RuntimeException("覆盖 Wiki 数据失败，Wiki ID 可能不存在");
        }
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
        return response;
    }
}
