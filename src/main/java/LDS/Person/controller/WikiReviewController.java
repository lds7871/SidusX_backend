package LDS.Person.controller;

import LDS.Person.config.BypassIpWhitelist;
import LDS.Person.dto.request.WikiReviewCreateRequest;
import LDS.Person.dto.request.WikiReviewPageQueryRequest;
import LDS.Person.dto.request.WikiReviewUpdateRequest;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.dto.response.WikiReviewListResponse;
import LDS.Person.dto.response.WikiReviewResponse;
import LDS.Person.service.WikiReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Wiki 审核控制层
 */
@RestController
@RequestMapping("/GHapi/wiki-review")
@Tag(name = "Wiki 审核管理")
public class WikiReviewController {

    private static final Logger log = LoggerFactory.getLogger(WikiReviewController.class);
    private final WikiReviewService wikiReviewService;

    public WikiReviewController(WikiReviewService wikiReviewService) {
        this.wikiReviewService = wikiReviewService;
    }

    /**
     * 提交 Wiki 审核申请
     */
    @PostMapping("/create")
    @BypassIpWhitelist(reason = "提交 Wiki 修改审核")
    @Operation(summary = "提交 Wiki 修改审核申请")
    public ResponseEntity<?> createReview(@RequestBody WikiReviewCreateRequest request) {
        try {
            log.info("提交 Wiki 审核 - WikiID: {}", request.getWikiId());
            WikiReviewResponse response = wikiReviewService.createReview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(JsonResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("提交审核失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("提交审核失败: " + e.getMessage()));
        }
    }

    /**
     * 更新审核状态
     * 状态 1: 通过并覆盖 Wiki 数据
     * 状态 2: 拒绝
     */
    @PostMapping("/update-status")
    @Operation(summary = "更新 Wiki 修改审核状态")
    public ResponseEntity<JsonResponse> updateStatus(@RequestBody WikiReviewUpdateRequest request) {
        try {
            log.info("更新审核状态 - ReviewID: {}, Status: {}", request.getWikireviewId(), request.getWikiStates());
            boolean success = wikiReviewService.updateReviewStatus(request);
            if (success) {
                String msg = request.getWikiStates() == 1 ? "审核通过，Wiki 数据已同步" : "审核已拒绝";
                return ResponseEntity.ok(JsonResponse.success(msg));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(JsonResponse.failure("审核记录不存在或更新失败"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(JsonResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("更新审核状态失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(JsonResponse.failure("系统错误"));
        }
    }

    /**
     * 分页查询 Wiki 审核记录
     */
    @PostMapping("/page-modify")
    @Operation(summary = "分页查询 Wiki 修改审核记录")
    public ResponseEntity<PageResponse<WikiReviewListResponse>> pageQuery(
            @RequestBody WikiReviewPageQueryRequest request) {
        try {
            log.info("分页查询 Wiki 审核 - Page: {}, PageSize: {}, Status: {}",
                    request.getPage(), request.getPageSize(), request.getWikiStates());
            PageResponse<WikiReviewListResponse> response = wikiReviewService.pageQuery(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("分页查询审核记录失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取 Wiki 审核详情
     */
    @GetMapping("/detail-modify/{wikireviewId}")
    @Operation(summary = "获取 Wiki 修改审核详情")
    public ResponseEntity<?> getReviewDetail(@PathVariable Long wikireviewId) {
        try {
            log.info("获取 Wiki 审核详情 - ReviewID: {}", wikireviewId);
            WikiReviewResponse response = wikiReviewService.getReviewDetail(wikireviewId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(JsonResponse.failure(e.getMessage()));
        } catch (Exception e) {
            log.error("获取审核详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("获取审核详情失败: " + e.getMessage()));
        }
    }
}