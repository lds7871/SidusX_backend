package LDS.Person.controller;

import LDS.Person.config.BypassIpWhitelist;
import LDS.Person.dto.request.WikiCommentByWikiIdRequest;
import LDS.Person.dto.request.WikiCommentCreateRequest;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.dto.response.WikiCommentResponse;
import LDS.Person.service.WikiCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Wiki留言控制层
 * 提供按 wiki_id 查询留言列表接口
 */
@RestController
@RequestMapping("/GHapi/wiki-comment")
@Tag(name = "Wiki 留言管理")
public class WikiCommentController {

    private static final Logger log = LoggerFactory.getLogger(WikiCommentController.class);

    private final WikiCommentService wikiCommentService;

    public WikiCommentController(WikiCommentService wikiCommentService) {
        this.wikiCommentService = wikiCommentService;
    }

    /**
     * 添加Wiki留言
     *
     * 请求体示例：
     * {
     * "wiki_id": 1,
     * "user_id": 2,
     * "text": "这是一条留言"
     * }
     *
     * @param request 添加留言请求
     * @return 添加结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加 Wiki 留言")
    public ResponseEntity<?> addComment(@RequestBody WikiCommentCreateRequest request) {
        try {
            if (request == null || request.getWikiId() == null || request.getUserId() == null
                    || request.getText() == null || request.getText().trim().isEmpty()) {
                log.warn("Wiki 留言添加参数为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(JsonResponse.failure("wiki_id、user_id、text 不能为空"));
            }

            log.info("添加 Wiki 留言 - WikiId: {}, UserId: {}", request.getWikiId(), request.getUserId());
            boolean success = wikiCommentService.addComment(request);
            if (!success) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(JsonResponse.failure("添加留言失败"));
            }

            return ResponseEntity.ok(JsonResponse.success("添加留言成功"));
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 留言添加参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Wiki 留言添加失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 留言添加失败"));
        }
    }

    /**
     * 根据 wiki_id 查询该 Wiki 下的所有留言
     * 返回留言内容、点赞数、留言时间，以及留言用户的 name 和 cover 字段
     *
     * 请求体示例：
     * {
     * "wiki_id": 1
     * }
     *
     * @param request 查询请求
     * @return 留言列表
     */
    @PostMapping("/list")
    // @BypassIpWhitelist(reason = "Wiki 留言查询接口")
    @Operation(summary = "根据 wiki_id 查询 Wiki 留言列表")
    public ResponseEntity<?> listByWikiId(@RequestBody WikiCommentByWikiIdRequest request) {
        try {
            if (request == null || request.getWikiId() == null) {
                log.warn("Wiki 留言查询参数为空");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(JsonResponse.failure("wiki_id 不能为空"));
            }

            log.info("查询 Wiki 留言列表 - WikiId: {}", request.getWikiId());
            List<WikiCommentResponse> response = wikiCommentService.getCommentsByWikiId(request);
            return ResponseEntity.ok(JsonResponse.success("查询成功", response));
        } catch (IllegalArgumentException ex) {
            log.warn("Wiki 留言查询参数验证失败: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(JsonResponse.failure(ex.getMessage()));
        } catch (Exception ex) {
            log.error("Wiki 留言查询失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("Wiki 留言查询失败"));
        }
    }
}
