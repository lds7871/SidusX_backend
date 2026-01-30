package LDS.Person.controller;

import LDS.Person.service.WikiService;
import io.swagger.v3.oas.annotations.Operation;
import LDS.Person.dto.request.WikiCreateRequest;
import LDS.Person.dto.request.WikiUpdateRequest;
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
@RequestMapping("/GHapi/wiki")
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
     * "key_name": "java_basics",
     * "texts": "Java 基础教程",
     * "tags": ["java", "programming", "basics"],
     * "create_user": "admin",
     * "update_user": "admin"
     * }
     * 
     * create_user 和 update_user 为可选字段，不提供时默认为 "system"
     * 
     * @param request 创建请求
     * @return 创建成功的 Wiki 响应
     */
    @PostMapping("/create")
    @BypassIpWhitelist(reason = "Wiki 创建接口")
    @Operation(summary = "创建新的 Wiki")
    public ResponseEntity<?> createWiki(@RequestBody WikiCreateRequest request) {
        try {
            log.info("创建 Wiki - KeyName: {}", request.getKeyName());
            WikiResponse response = wikiService.createWiki(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 创建参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Wiki 创建失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 创建失败: " + ex.getMessage()));
        }
    }

    /**
     * 删除 Wiki 记录
     * 
     * @param wikiId Wiki ID
     * @return JSON 格式的删除结果
     */
    @DeleteMapping("/{wikiId}")
    @Operation(summary = "删除指定 ID 的 Wiki")
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
     * "page": 1,
     * "page_size": 10,
     * "key_name": "java",
     * "tags": "programming",
     * "create_time_start": "2025-01-01 00:00:00",
     * "create_time_end": "2025-12-31 23:59:59"
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
    @Operation(summary = "分页查询 Wiki 列表")
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

    /**
     * 查询 Wiki 名称重复性
     * 检查给定的 key_name 是否已在数据库中存在
     * 
     * 请求示例：
     * GET /GHapi/wiki/check-key-name?keyName=java_basics
     * 
     * 响应示例（存在）：
     * {
     * "exists": true,
     * "message": "已有此名称！"
     * }
     * 
     * 响应示例（不存在）：
     * {
     * "exists": false,
     * "message": "可创建的Wiki名称"
     * }
     * 
     * @param keyName Wiki 键名
     * @return JSON 响应，包含存在状态和提示信息
     */
    @GetMapping("/check-key-name")
    @Operation(summary = "检查 Wiki 名称是否已存在")
    @BypassIpWhitelist(reason = "Wiki 名称检查接口")
    public ResponseEntity<?> checkKeyName(@RequestParam String keyName) {
        try {
            if (keyName == null || keyName.trim().isEmpty()) {
                log.warn("Wiki 键名为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(JsonResponse.failure("Wiki 键名不能为空"));
            }

            log.info("检查 Wiki 键名 - KeyName: {}", keyName);
            boolean exists = wikiService.isKeyNameExists(keyName.trim());

            if (exists) {
                return ResponseEntity.ok(new NameCheckResponse(true, "已有此名称！"));
            } else {
                return ResponseEntity.ok(new NameCheckResponse(false, "可创建的Wiki名称"));
            }
        } catch (Exception ex) {
            log.error("Wiki 名称检查失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 名称检查失败"));
        }
    }

    /**
     * 根据 Wiki ID 查询完整内容
     * 
     * 请求示例：
     * GET /GHapi/wiki/1
     * 
     * 响应示例（成功）：
     * {
     * "wiki_id": 1,
     * "key_name": "java_basics",
     * "texts": "Java 基础教程内容...",
     * "tags": ["java", "programming"],
     * "version": 1.0,
     * "create_time": "2025-01-01 10:00:00",
     * "create_user": "admin",
     * "update_time": "2025-01-01 10:00:00",
     * "update_user": "admin"
     * }
     * 
     * @param wikiId Wiki ID
     * @return Wiki 完整内容，如果不存在则返回 404
     */
    @GetMapping("/{wikiId}")
    @Operation(summary = "根据 ID 查询 Wiki 完整内容")
    @BypassIpWhitelist(reason = "Wiki 详情查询接口")
    public ResponseEntity<?> getWikiById(@PathVariable Long wikiId) {
        try {
            log.info("查询 Wiki 详情 - ID: {}", wikiId);
            WikiResponse response = wikiService.getWikiById(wikiId);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(JsonResponse.failure("Wiki 不存在"));
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 查询参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Wiki 查询失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 查询失败"));
        }
    }

    /**
     * 更新 Wiki 记录
     * 
     * 请求体示例：
     * {
     * "texts": "更新后的 Java 基础教程内容",
     * "tags": ["java", "programming", "updated"],
     * "version": 1.10,
     * "update_user": "admin"
     * }
     * 
     * 字段说明：
     * - texts: Wiki 内容（可选）
     * - tags: 标签数组（可选）
     * - version: 版本号（可选）
     * - update_user: 更新用户（必填）
     * - update_time: 自动设置为当前时间
     * 
     * 至少需要提供 texts、tags 或 version 其中一个字段进行更新
     * 
     * @param wikiId  Wiki ID
     * @param request 更新请求
     * @return 更新后的 Wiki 响应
     */
    @PutMapping("/{wikiId}")
    @Operation(summary = "更新指定 ID 的 Wiki")
    public ResponseEntity<?> updateWiki(@PathVariable Long wikiId, @RequestBody WikiUpdateRequest request) {
        try {
            log.info("更新 Wiki - ID: {}, UpdateUser: {}", wikiId, request.getUpdateUser());
            WikiResponse response = wikiService.updateWiki(wikiId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 更新参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Wiki 更新失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 更新失败: " + ex.getMessage()));
        }
    }

    /**
     * Wiki 名称检查响应类
     */
    public static class NameCheckResponse {
        private boolean exists;
        private String message;

        public NameCheckResponse(boolean exists, String message) {
            this.exists = exists;
            this.message = message;
        }

        public boolean isExists() {
            return exists;
        }

        public void setExists(boolean exists) {
            this.exists = exists;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
