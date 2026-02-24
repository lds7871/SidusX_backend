package LDS.Person.service.impl;

import LDS.Person.entity.WikiNew;
import LDS.Person.repository.WikiNewMapper;
import LDS.Person.service.WikiNewService;
import LDS.Person.dto.request.WikiNewCreateRequest;
import LDS.Person.dto.request.WikiNewPageQueryRequest;
import LDS.Person.dto.response.WikiNewResponse;
import LDS.Person.dto.response.WikiNewListResponse;
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
import java.util.Arrays;
import java.util.List;

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
    String[] tagsArray = listToArray(request.getTags());
    WikiNew wikiNew = new WikiNew(
        request.getKeyName(),
        request.getTexts(),
        tagsArray,
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

    WikiNew wikiNew = wikiNewMapper.selectWikiNewById(wikinewId);
    if (wikiNew == null) {
      log.debug("Wiki 新增不存在 - ID: {}", wikinewId);
      return null;
    }

    log.info("查询 Wiki 新增完整内容 - ID: {}, KeyName: {}", wikinewId, wikiNew.getKeyName());
    return convertToResponse(wikiNew);
  }

  /**
   * 检查 Wiki 键名是否已存在
   */
  @Override
  public boolean isKeyNameExists(String keyName) {
    if (keyName == null || keyName.trim().isEmpty()) {
      return false;
    }
    WikiNew wikiNew = wikiNewMapper.selectByKeyName(keyName.trim());
    return wikiNew != null;
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
    response.setTags(arrayToList(wikiNew.getTags()));
    response.setVersion(wikiNew.getVersion());
    response.setCreateTime(wikiNew.getCreateTime() != null ? wikiNew.getCreateTime().format(DATE_FORMATTER) : null);
    response.setCreateUser(wikiNew.getCreateUser());
    response.setUpdateTime(wikiNew.getUpdateTime() != null ? wikiNew.getUpdateTime().format(DATE_FORMATTER) : null);
    response.setUpdateUser(wikiNew.getUpdateUser());
    response.setWikiStates(wikiNew.getWikiStates());
    return response;
  }

  /**
   * 将数组转换为列表
   */
  private List<String> arrayToList(String[] array) {
    if (array == null || array.length == 0) {
      return new ArrayList<>();
    }
    return Arrays.asList(array);
  }

  /**
   * 将列表转换为数组
   */
  private String[] listToArray(List<String> list) {
    if (list == null || list.isEmpty()) {
      return new String[0];
    }
    return list.toArray(new String[0]);
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
        String[] tagsArrayValue = (String[]) tagsArray.getArray();
        response.setTags(Arrays.asList(tagsArrayValue));
      } else {
        response.setTags(new ArrayList<>());
      }

      response.setCreateUser(rs.getString("create_user"));
      response.setWikiStates(rs.getInt("wiki_states"));
      return response;
    }
  }
}
