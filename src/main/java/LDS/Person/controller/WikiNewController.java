package LDS.Person.controller;

import LDS.Person.service.WikiNewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import LDS.Person.dto.request.WikiNewCreateRequest;
import LDS.Person.dto.request.WikiNewPageQueryRequest;
import LDS.Person.dto.request.WikiNewReviewRequest;
import LDS.Person.dto.response.WikiNewResponse;
import LDS.Person.dto.response.WikiNewListResponse;
import LDS.Person.dto.response.WikiNewReviewResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.config.BypassIpWhitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Wiki 新增控制层
 * 提供 Wiki 新增的增、查接口，审核状态为 0 时内容不可修改
 */
@RestController
@RequestMapping("/GHapi/wiki-new")
@Tag(name = "Wiki 新增管理")
public class WikiNewController {

  private static final Logger log = LoggerFactory.getLogger(WikiNewController.class);

  private final WikiNewService wikiNewService;

  public WikiNewController(WikiNewService wikiNewService) {
    this.wikiNewService = wikiNewService;
  }

  /**
   * 新增 Wiki 记录
   * 
   * 请求体示例：
   * {
   * "key_name": "java_basics",
   * "texts": "Java 基础教程",
   * "tags": ["java", "programming", "basics"],
   * "create_user": "admin"
   * }
   * 
   * 说明：
   * - wikinew_id 自动生成，不能修改
   * - version 默认为 1.00，不能修改
   * - wiki_states 默认为 0（待审核），不能修改
   * - create_user 和 update_user 使用同一个字段值
   * 
   * @param request 创建请求
   * @return 创建成功的 Wiki 响应
   */
  @PostMapping("/create")
  @BypassIpWhitelist(reason = "Wiki 新增创建接口")
  @Operation(summary = "创建新的 Wiki 审核申请")
  public ResponseEntity<?> createWikiNew(@RequestBody WikiNewCreateRequest request) {
    try {
      log.info("创建 Wiki 新增 - KeyName: {}", request.getKeyName());
      WikiNewResponse response = wikiNewService.createWikiNew(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException ex) {
      log.warn("Wiki 新增创建参数验证失败: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(JsonResponse.failure(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Wiki 新增创建失败", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(JsonResponse.failure("Wiki 新增创建失败: " + ex.getMessage()));
    }
  }

  /**
   * 分页查询 Wiki 新增列表
   * 支持按 wiki_states 和 wikinew_id 进行过滤
   * 
   * 请求体示例：
   * {
   * "page": 1,
   * "page_size": 10,
   * "wiki_states": 0,
   * "wikinew_id": null
   * }
   * 
   * 返回字段说明：
   * - wikinew_id: Wiki ID
   * - key_name: Wiki 键名
   * - tags: 标签数组
   * - create_user: 创建用户
   * - wiki_states: 审核状态（0：待审核，1：通过，2：拒绝）
   * 
   * @param request 分页查询请求
   * @return 分页响应
   */
  @PostMapping("/page")
  @Operation(summary = "分页查询 Wiki 新增列表")
  public ResponseEntity<PageResponse<WikiNewListResponse>> pageQuery(@RequestBody WikiNewPageQueryRequest request) {
    try {
      log.info("分页查询 Wiki 新增 - Page: {}, PageSize: {}", request.getPage(), request.getPageSize());
      PageResponse<WikiNewListResponse> response = wikiNewService.pageQuery(request);
      return ResponseEntity.ok(response);
    } catch (Exception ex) {
      log.error("Wiki 新增分页查询失败", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * 根据 Wiki ID 查询完整内容
   * 返回所有字段信息
   * 
   * 请求示例：
   * GET /GHapi/wiki-new/1
   * 
   * 响应示例（成功）：
   * {
   * "wikinew_id": 1,
   * "key_name": "java_basics",
   * "texts": "Java 基础教程内容...",
   * "tags": ["java", "programming"],
   * "version": 1.00,
   * "create_time": "2025-01-01 10:00:00",
   * "create_user": "admin",
   * "update_time": "2025-01-01 10:00:00",
   * "update_user": "admin",
   * "wiki_states": 0
   * }
   * 
   * @param wikinewId Wiki ID
   * @return Wiki 完整内容，如果不存在则返回 404
   */
  @GetMapping("/{wikinewId}")
  @Operation(summary = "根据 ID 查询 Wiki 新增完整内容")
  public ResponseEntity<?> getWikiNewById(@PathVariable Long wikinewId) {
    try {
      log.info("查询 Wiki 新增详情 - ID: {}", wikinewId);
      WikiNewResponse response = wikiNewService.getWikiNewById(wikinewId);

      if (response == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(JsonResponse.failure("Wiki 新增不存在"));
      }

      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException ex) {
      log.warn("Wiki 新增查询参数验证失败: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(JsonResponse.failure(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Wiki 新增查询失败", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(JsonResponse.failure("Wiki 新增查询失败"));
    }
  }

  /**
   * 审核 Wiki 新增申请
   * 批准（wiki_states=1）时将内容复制到 wiki 主表
   * 驳回（wiki_states=2）时仅更新状态
   * 
   * 请求体示例：
   * {
   * "wikinew_id": 1,
   * "wiki_states": 1
   * }
   * 
   * 响应示例（批准成功）：
   * {
   * "wikinew_id": 1,
   * "wiki_states": 1,
   * "message": "Wiki 新增已批准并添加到主表",
   * "generated_wiki_id": 123
   * }
   * 
   * @param request 审核请求
   * @return 审核响应
   */
  @PostMapping("/review")
  @Operation(summary = "审核 Wiki 新增申请（批准或驳回）")
  public ResponseEntity<?> reviewWikiNew(@RequestBody WikiNewReviewRequest request) {
    try {
      log.info("审核 Wiki 新增 - ID: {}, 审核状态: {}", request.getWikinewId(), request.getWikiStates());
      WikiNewReviewResponse response = wikiNewService.reviewWikiNew(request);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException ex) {
      log.warn("Wiki 新增审核参数验证失败: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(JsonResponse.failure(ex.getMessage()));
    } catch (Exception ex) {
      log.error("Wiki 新增审核失败", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(JsonResponse.failure("Wiki 新增审核失败: " + ex.getMessage()));
    }
  }

  /**
   * Wiki 键名检查响应类
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
