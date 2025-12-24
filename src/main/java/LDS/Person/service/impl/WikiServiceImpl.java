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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Wiki 业务逻辑实现类
 */
@Service
public class WikiServiceImpl implements WikiService {
    
    private static final Logger log = LoggerFactory.getLogger(WikiServiceImpl.class);
    
    private static final String DEFAULT_USER = "system";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final WikiMapper wikiMapper;
    
    public WikiServiceImpl(WikiMapper wikiMapper) {
        this.wikiMapper = wikiMapper;
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
        Wiki existingWiki = wikiMapper.selectByKeyName(request.getKeyName());
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
        Wiki wiki = wikiMapper.selectById(wikiId);
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
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大页面大小
        }
        
        // 计算偏移量
        int offset = (page - 1) * pageSize;
        
        // 查询总数
        long totalCount = wikiMapper.countByCondition(request);
        
        // 查询分页数据
        List<Wiki> wikiList = wikiMapper.selectPageList(request, offset);
        
        // 转换为响应对象
        List<WikiResponse> responseList = new ArrayList<>();
        for (Wiki wiki : wikiList) {
            responseList.add(convertToResponse(wiki));
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
}
