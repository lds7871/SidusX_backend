package LDS.Person.controller;

import LDS.Person.service.WikiService;
import LDS.Person.dto.request.WikiCreateRequest;
import LDS.Person.dto.request.WikiPageQueryRequest;
import LDS.Person.dto.response.WikiResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.config.BypassIpWhitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Wiki 控制层
 * 提供 Wiki 的增、删、查接口
 */
@RestController
@RequestMapping("/api/wiki")
public class WikiController {
    
    private static final Logger log = LoggerFactory.getLogger(WikiController.class);
    
    private final WikiService wikiService;
    
    public WikiController(WikiService wikiService) {
        this.wikiService = wikiService;
    }
    
    /**
     * 新增 Wiki 记录
     * 
     * 请求体示例：
     * {
     *   "key_name": "java_basics",
     *   "texts": "Java 基础教程",
     *   "tags": ["java", "programming", "basics"],
     *   "create_user": "admin",
     *   "update_user": "admin"
     * }
     * 
     * create_user 和 update_user 为可选字段，不提供时默认为 "system"
     * 
     * @param request 创建请求
     * @return 创建成功的 Wiki 响应
     */
    @PostMapping("/create")
    @BypassIpWhitelist(reason = "Wiki 创建接口")
    public ResponseEntity<WikiResponse> createWiki(@RequestBody WikiCreateRequest request) {
        try {
            log.info("创建 Wiki - KeyName: {}", request.getKeyName());
            WikiResponse response = wikiService.createWiki(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 创建参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception ex) {
            log.error("Wiki 创建失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除 Wiki 记录
     * 
     * @param wikiId Wiki ID
     * @return JSON 格式的删除结果
     */
    @DeleteMapping("/{wikiId}")
    @BypassIpWhitelist(reason = "Wiki 删除接口")
    public ResponseEntity<JsonResponse> deleteWiki(@PathVariable Long wikiId) {
        try {
            log.info("删除 Wiki - ID: {}", wikiId);
            boolean success = wikiService.deleteWiki(wikiId);
            if (success) {
                return ResponseEntity.ok(JsonResponse.success("Wiki 删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(JsonResponse.failure("Wiki 不存在"));
            }
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 删除参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Wiki 删除失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 删除失败"));
        }
    }
    
    /**
     * 分页查询 Wiki 列表
     * 支持多条件过滤和排序
     * 
     * 请求体示例：
     * {
     *   "page": 1,
     *   "page_size": 10,
     *   "key_name": "java",
     *   "tags": "programming",
     *   "create_time_start": "2025-01-01 00:00:00",
     *   "create_time_end": "2025-12-31 23:59:59"
     * }
     * 
     * 字段说明：
     * - page: 当前页码（默认 1）
     * - page_size: 每页数量（默认 10，最大 100）
     * - key_name: Wiki 键名（模糊匹配，可选）
     * - tags: 标签（模糊匹配，可选）
     * - create_time_start: 创建时间开始（ISO 8601 格式，可选）
     * - create_time_end: 创建时间结束（ISO 8601 格式，可选）
     * 
     * @param request 分页查询请求（JSON 格式）
     * @return 分页响应
     */
    @PostMapping("/page")
    @BypassIpWhitelist(reason = "Wiki 分页查询接口")
    public ResponseEntity<PageResponse<WikiResponse>> pageQuery(@RequestBody WikiPageQueryRequest request) {
        try {
            log.info("分页查询 Wiki - Page: {}, PageSize: {}", request.getPage(), request.getPageSize());
            PageResponse<WikiResponse> response = wikiService.pageQuery(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Wiki 分页查询失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
