package LDS.Person.service.impl;

import LDS.Person.entity.WikiNew;
import LDS.Person.repository.WikiNewMapper;
import LDS.Person.service.WikiNewService;
import LDS.Person.dto.request.WikiNewCreateRequest;
import LDS.Person.dto.request.WikiNewPageQueryRequest;
import LDS.Person.dto.request.WikiNewReviewRequest;
import LDS.Person.dto.response.WikiNewResponse;
import LDS.Person.dto.response.WikiNewListResponse;
import LDS.Person.dto.response.WikiNewReviewResponse;
import LDS.Person.dto.response.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * Wiki 新增业务逻辑实现类
 */
@Service
public class WikiNewServiceImpl implements WikiNewService {

  private static final Logger log = LoggerFactory.getLogger(WikiNewServiceImpl.class);

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final WikiNewMapper wikiNewMapper;
  private final JdbcTemplate jdbcTemplate;

  public WikiNewServiceImpl(WikiNewMapper wikiNewMapper, JdbcTemplate jdbcTemplate) {
    this.wikiNewMapper = wikiNewMapper;
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * 新增 Wiki 记录
   * version 默认为 1.00, wiki_states 默认为 0, create_user 和 update_user 使用同一个值
   */
  @Override
  @Transactional
  public WikiNewResponse createWikiNew(WikiNewCreateRequest request) {
    // 验证必填字段
    if (request.getKeyName() == null || request.getKeyName().trim().isEmpty()) {
      log.warn("Wiki 键名为空");
      throw new IllegalArgumentException("Wiki 键名不能为空");
    }

    if (request.getTexts() == null || request.getTexts().trim().isEmpty()) {
      log.warn("Wiki 内容为空");
      throw new IllegalArgumentException("Wiki 内容不能为空");
    }

    if (request.getCreateUser() == null || request.getCreateUser().trim().isEmpty()) {
      log.warn("创建用户为空");
      throw new IllegalArgumentException("创建用户不能为空");
    }

    // 检查键名唯一性
    WikiNew existingWikiNew = wikiNewMapper.selectByKeyName(request.getKeyName());
    if (existingWikiNew != null) {
      log.warn("Wiki 键名已存在: {}", request.getKeyName());
      throw new IllegalArgumentException("Wiki 键名已存在: " + request.getKeyName());
    }

    // 创建 WikiNew 实体
    WikiNew wikiNew = new WikiNew(
        request.getKeyName(),
        request.getTexts(),
        request.getTags(),
        request.getCreateUser().trim());

    // 保存到数据库
    int result = wikiNewMapper.insertWikiNew(wikiNew);
    if (result <= 0) {
      log.error("Wiki 新增保存失败: {}", request.getKeyName());
      throw new RuntimeException("Wiki 新增保存失败");
    }

    log.info("Wiki 新增成功 - ID: {}, KeyName: {}, CreateUser: {}",
        wikiNew.getWikinewId(), wikiNew.getKeyName(), wikiNew.getCreateUser());

    return convertToResponse(wikiNew);
  }

  /**
   * 分页查询 Wiki 新增列表
   */
  @Override
  public PageResponse<WikiNewListResponse> pageQuery(WikiNewPageQueryRequest request) {
    int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
    int pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;

    if (pageSize > 100) {
      pageSize = 100;
    }

    int offset = (page - 1) * pageSize;

    // 查询总数
    long totalCount = countByCondition(request);

    // 查询分页数据
    List<WikiNewListResponse> responseList = selectPageList(request, offset, pageSize);

    // 计算总页数
    long totalPages = (totalCount + pageSize - 1) / pageSize;

    log.debug("Wiki 新增分页查询 - 页码: {}, 每页: {}, 总数: {}", page, pageSize, totalCount);

    return new PageResponse<>(page, pageSize, totalCount, totalPages, responseList);
  }

  /**
   * 根据 Wiki ID 查询完整内容
   */
  @Override
  public WikiNewResponse getWikiNewById(Long wikinewId) {
    if (wikinewId == null || wikinewId <= 0) {
      log.warn("Wiki ID 无效: {}", wikinewId);
      return null;
    }

    String sql = "SELECT wikinew_id, key_name, texts, tags, version, " +
        "create_time, create_user, update_time, update_user, wiki_states " +
        "FROM wiki_new WHERE wikinew_id = ?";

    List<WikiNewResponse> results = jdbcTemplate.query(sql, new Object[] { wikinewId },
        (rs, rowNum) -> {
          WikiNewResponse response = new WikiNewResponse();
          response.setWikinewId(rs.getLong("wikinew_id"));
          response.setKeyName(rs.getString("key_name"));
          response.setTexts(rs.getString("texts"));

          // 处理 PostgreSQL 数组
          java.sql.Array tagsArray = rs.getArray("tags");
          if (tagsArray != null) {
            response.setTags((String[]) tagsArray.getArray());
          }

          response.setVersion(rs.getDouble("version"));
          response.setCreateTime(rs.getTimestamp("create_time") != null
              ? rs.getTimestamp("create_time").toLocalDateTime().format(DATE_FORMATTER)
              : null);
          response.setCreateUser(rs.getString("create_user"));
          response.setUpdateTime(rs.getTimestamp("update_time") != null
              ? rs.getTimestamp("update_time").toLocalDateTime().format(DATE_FORMATTER)
              : null);
          response.setUpdateUser(rs.getString("update_user"));
          response.setWikiStates(rs.getInt("wiki_states"));

          return response;
        });

    if (results.isEmpty()) {
      log.debug("Wiki 新增不存在 - ID: {}", wikinewId);
      return null;
    }

    WikiNewResponse response = results.get(0);
    log.info("查询 Wiki 新增完整内容 - ID: {}, KeyName: {}", wikinewId, response.getKeyName());
    return response;
  }

  /**
   * 检查 Wiki 键名是否已存在于 wiki 主表中
   */
  @Override
  public boolean isKeyNameExists(String keyName) {
    if (keyName == null || keyName.trim().isEmpty()) {
      return false;
    }
    String sql = "SELECT COUNT(*) FROM wiki WHERE key_name = ?";
    Long count = jdbcTemplate.queryForObject(sql, new Object[] { keyName.trim() }, Long.class);
    return count != null && count > 0;
  }

  /**
   * 审核 Wiki 新增申请
   * 批准（wikiStates=1）时将内容复制到 wiki 表
   * 驳回（wikiStates=2）时仅更新状态
   */
  @Override
  @Transactional
  public WikiNewReviewResponse reviewWikiNew(WikiNewReviewRequest request) {
    // 验证参数
    if (request.getWikinewId() == null || request.getWikinewId() <= 0) {
      log.warn("Wiki 新增 ID 无效: {}", request.getWikinewId());
      throw new IllegalArgumentException("Wiki 新增 ID 无效");
    }

    if (request.getWikiStates() == null || (request.getWikiStates() != 1 && request.getWikiStates() != 2)) {
      log.warn("审核状态无效，只能为 1（批准）或 2（驳回）：{}", request.getWikiStates());
      throw new IllegalArgumentException("审核状态只能为 1（批准）或 2（驳回）");
    }

    // 查询 Wiki 新增记录
    String selectSql = "SELECT wikinew_id, key_name, texts, tags, wiki_states " +
        "FROM wiki_new WHERE wikinew_id = ?";
    List<WikiNewData> results = jdbcTemplate.query(selectSql, new Object[] { request.getWikinewId() },
        (rs, rowNum) -> {
          WikiNewData data = new WikiNewData();
          data.setWikinewId(rs.getLong("wikinew_id"));
          data.setKeyName(rs.getString("key_name"));
          data.setTexts(rs.getString("texts"));
          java.sql.Array tagsArray = rs.getArray("tags");
          if (tagsArray != null) {
            data.setTags((String[]) tagsArray.getArray());
          }
          data.setWikiStates(rs.getInt("wiki_states"));
          return data;
        });

    if (results.isEmpty()) {
      log.warn("Wiki 新增不存在 - ID: {}", request.getWikinewId());
      throw new IllegalArgumentException("Wiki 新增不存在");
    }

    WikiNewData wikiNewData = results.get(0);

    // 验证当前状态为 0（待审核）
    if (wikiNewData.getWikiStates() != 0) {
      log.warn("Wiki 新增已审核，无法再次审核 - ID: {}, 当前状态: {}", request.getWikinewId(), wikiNewData.getWikiStates());
      throw new IllegalArgumentException("该 Wiki 新增已审核，无法再次修改状态");
    }

    // 更新 wiki_new 表的状态
    String updateSql = "UPDATE wiki_new SET wiki_states = ?, update_time = NOW() WHERE wikinew_id = ?";
    int updateResult = jdbcTemplate.update(updateSql, request.getWikiStates(), request.getWikinewId());

    if (updateResult <= 0) {
      log.error("更新 Wiki 新增状态失败 - ID: {}", request.getWikinewId());
      throw new RuntimeException("更新状态失败");
    }

    log.info("Wiki 新增审核状态已更新 - ID: {}, 新状态: {}", request.getWikinewId(), request.getWikiStates());

    // 如果批准（状态为 1），则复制到 wiki 表
    if (request.getWikiStates() == 1) {
      return approveWikiNew(wikiNewData);
    }

    // 驳回（状态为 2）
    return new WikiNewReviewResponse(request.getWikinewId(), 2, "Wiki 新增已驳回");
  }

  /**
   * 批准 Wiki 新增，复制到 wiki 表
   */
  private WikiNewReviewResponse approveWikiNew(WikiNewData wikiNewData) {
    // 验证键名唯一性（防止重复）
    if (isKeyNameExists(wikiNewData.getKeyName())) {
      log.warn("Wiki 键名已在 wiki 表中存在，不能批准 - KeyName: {}", wikiNewData.getKeyName());
      throw new IllegalArgumentException("该 Wiki 键名已存在，无法批准");
    }

    // 构建 PostgreSQL 数组格式：{val1,val2}（不需要外层单引号）
    String tagsArray = null;
    if (wikiNewData.getTags() != null && wikiNewData.getTags().length > 0) {
      StringBuilder sb = new StringBuilder("{");
      for (int i = 0; i < wikiNewData.getTags().length; i++) {
        if (i > 0)
          sb.append(",");
        // PostgreSQL 数组字符串中需要用双引号包围，并转义内部的双引号
        String tag = wikiNewData.getTags()[i].replace("\"", "\\\"");
        sb.append("\"").append(tag).append("\"");
      }
      sb.append("}");
      tagsArray = sb.toString();
    } else {
      tagsArray = "{}"; // 空数组
    }

    log.debug("构建的 PostgreSQL 数组格式: {}", tagsArray);

    // 使用原生 SQL 通过 CAST 插入数组
    String insertSql = "INSERT INTO wiki (key_name, texts, tags, version, create_time, create_user, update_time, update_user) "
        + "VALUES (?, ?, ?::text[], ?, NOW(), ?, NOW(), ?) RETURNING wiki_id";

    Long generatedWikiId = jdbcTemplate.queryForObject(insertSql,
        new Object[] {
            wikiNewData.getKeyName(),
            wikiNewData.getTexts(),
            tagsArray,
            1.00,
            wikiNewData.getKeyName(), // 创建用户
            wikiNewData.getKeyName() // 更新用户
        },
        Long.class);

    log.info("Wiki 新增已批准并复制到 wiki 表 - 原 ID: {}, 新 Wiki ID: {}", wikiNewData.getWikinewId(), generatedWikiId);

    return new WikiNewReviewResponse(wikiNewData.getWikinewId(), 1, "Wiki 新增已批准并添加到主表", generatedWikiId);
  }

  /**
   * 临时数据类，用于存储查询结果
   */
  private static class WikiNewData {
    private Long wikinewId;
    private String keyName;
    private String texts;
    private String[] tags;
    private Integer wikiStates;

    // Getters and Setters
    public Long getWikinewId() {
      return wikinewId;
    }

    public void setWikinewId(Long wikinewId) {
      this.wikinewId = wikinewId;
    }

    public String getKeyName() {
      return keyName;
    }

    public void setKeyName(String keyName) {
      this.keyName = keyName;
    }

    public String getTexts() {
      return texts;
    }

    public void setTexts(String texts) {
      this.texts = texts;
    }

    public String[] getTags() {
      return tags;
    }

    public void setTags(String[] tags) {
      this.tags = tags;
    }

    public Integer getWikiStates() {
      return wikiStates;
    }

    public void setWikiStates(Integer wikiStates) {
      this.wikiStates = wikiStates;
    }
  }

  /**
   * 查询满足条件的记录总数
   */
  private long countByCondition(WikiNewPageQueryRequest request) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM wiki_new WHERE 1=1 ");
    List<Object> params = new ArrayList<>();

    // 审核状态过滤
    if (request.getWikiStates() != null) {
      sql.append("AND wiki_states = ? ");
      params.add(request.getWikiStates());
    }

    // Wiki ID 过滤
    if (request.getWikinewId() != null && request.getWikinewId() > 0) {
      sql.append("AND wikinew_id = ? ");
      params.add(request.getWikinewId());
    }

    Long count = jdbcTemplate.queryForObject(sql.toString(), params.toArray(new Object[0]), Long.class);
    return count != null ? count : 0;
  }

  /**
   * 分页查询 Wiki 列表（仅返回必要字段）
   */
  private List<WikiNewListResponse> selectPageList(WikiNewPageQueryRequest request, int offset, int pageSize) {
    StringBuilder sql = new StringBuilder();
    List<Object> params = new ArrayList<>();

    sql.append("SELECT wikinew_id, key_name, tags, create_user, wiki_states ");
    sql.append("FROM wiki_new WHERE 1=1 ");

    // 审核状态过滤
    if (request.getWikiStates() != null) {
      sql.append("AND wiki_states = ? ");
      params.add(request.getWikiStates());
    }

    // Wiki ID 过滤
    if (request.getWikinewId() != null && request.getWikinewId() > 0) {
      sql.append("AND wikinew_id = ? ");
      params.add(request.getWikinewId());
    }

    // 排序和分页
    sql.append("ORDER BY create_time DESC ");
    sql.append("LIMIT ? OFFSET ?");
    params.add(pageSize);
    params.add(offset);

    return jdbcTemplate.query(sql.toString(), params.toArray(new Object[0]), new WikiNewListRowMapper());
  }

  /**
   * 将 WikiNew 实体转换为完整响应对象
   */
  private WikiNewResponse convertToResponse(WikiNew wikiNew) {
    WikiNewResponse response = new WikiNewResponse();
    response.setWikinewId(wikiNew.getWikinewId());
    response.setKeyName(wikiNew.getKeyName());
    response.setTexts(wikiNew.getTexts());
    response.setTags(wikiNew.getTags());
    response.setVersion(wikiNew.getVersion());
    response.setCreateTime(wikiNew.getCreateTime() != null ? wikiNew.getCreateTime().format(DATE_FORMATTER) : null);
    response.setCreateUser(wikiNew.getCreateUser());
    response.setUpdateTime(wikiNew.getUpdateTime() != null ? wikiNew.getUpdateTime().format(DATE_FORMATTER) : null);
    response.setUpdateUser(wikiNew.getUpdateUser());
    response.setWikiStates(wikiNew.getWikiStates());
    return response;
  }

  /**
   * Wiki 新增列表行映射器
   */
  public static class WikiNewListRowMapper implements RowMapper<WikiNewListResponse> {
    @Override
    public WikiNewListResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
      WikiNewListResponse response = new WikiNewListResponse();
      response.setWikinewId(rs.getLong("wikinew_id"));
      response.setKeyName(rs.getString("key_name"));

      // 处理 PostgreSQL 数组
      java.sql.Array tagsArray = rs.getArray("tags");
      if (tagsArray != null) {
        response.setTags((String[]) tagsArray.getArray());
      }

      response.setCreateUser(rs.getString("create_user"));
      response.setWikiStates(rs.getInt("wiki_states"));
      return response;
    }
  }
}
