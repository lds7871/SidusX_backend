package LDS.Person.controller;

import LDS.Person.dto.request.NasaDailyImagePageQueryRequest;
import LDS.Person.dto.response.NasaDailyImageListResponse;
import LDS.Person.dto.response.NasaDailyImageDetailResponse;
import LDS.Person.dto.response.PageResponse;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.service.NasaDailyImageService;
import LDS.Person.config.BypassIpWhitelist;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * NASA 每日图片控制层
 * 提供NASA APOD图片的查询、删除接口
 */
@RestController
@RequestMapping("/GHapi/nasa-daily-image")
@Tag(name = "NASA 每日图片", description = "NASA APOD 图片的查询与删除接口")
public class NasaDailyImageController {

    private static final Logger log = LoggerFactory.getLogger(NasaDailyImageController.class);

    private final NasaDailyImageService nasaDailyImageService;

    public NasaDailyImageController(NasaDailyImageService nasaDailyImageService) {
        this.nasaDailyImageService = nasaDailyImageService;
    }

    /**
     * 分页查询NASA每日图片列表
     * 支持按标题和时间范围筛选
     * 
     * 请求体示例：
     * {
     * "page": 1,
     * "page_size": 10,
     * "title": "Galaxy",
     * "create_time_start": "2025-01-01 00:00:00",
     * "create_time_end": "2025-12-31 23:59:59"
     * }
     * 
     * 字段说明：
     * - page: 当前页码（默认 1）
     * - page_size: 每页数量（默认 10，最大 100）
     * - title: 图片标题（模糊匹配，可选）
     * - create_time_start: 创建时间开始（ISO 8601 格式，可选）
     * - create_time_end: 创建时间结束（ISO 8601 格式，可选）
     * 
     * 返回示例：
     * {
     * "page": 1,
     * "page_size": 10,
     * "total": 50,
     * "total_pages": 5,
     * "data": [
     * {
     * "apod_id": 1,
     * "title": "The Pillars of Creation",
     * "create_time": "2025-01-05 10:30:00"
     * }
     * ]
     * }
     * 
     * @param request 分页查询请求
     * @return 分页响应，包含apod_id、title、create_time三个字段
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询NASA每日图片列表")
    public ResponseEntity<PageResponse<NasaDailyImageListResponse>> pageQuery(
            @RequestBody NasaDailyImagePageQueryRequest request) {
        try {
            log.info("分页查询NASA图片 - Page: {}, PageSize: {}", request.getPage(), request.getPageSize());
            PageResponse<NasaDailyImageListResponse> response = nasaDailyImageService.pageQuery(request);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("NASA图片分页查询失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取NASA图片详细信息
     * 返回完整的图片信息，包括版权信息、说明文字、图片链接等
     * 
     * 请求示例：
     * GET /GHapi/nasa-daily-image/1
     * 
     * 返回示例：
     * {
     * "apod_id": 1,
     * "copyright": "NASA",
     * "explanation": "This is a detailed explanation of the image...",
     * "media_type": "image",
     * "title": "The Pillars of Creation",
     * "url": "https://apod.nasa.gov/apod/image/2501/...",
     * "create_time": "2025-01-05 10:30:00"
     * }
     * 
     * @param apodId NASA图片ID
     * @return 图片详细信息响应
     */
    @GetMapping("/{apodId}")
    @Operation(summary = "获取NASA图片详细信息")
    public ResponseEntity<?> getDetail(@PathVariable Long apodId) {
        try {
            if (apodId == null || apodId <= 0) {
                log.warn("图片ID无效: {}", apodId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(JsonResponse.failure("图片ID无效"));
            }

            log.info("查询NASA图片详情 - ID: {}", apodId);
            NasaDailyImageDetailResponse response = nasaDailyImageService.getDetail(apodId);

            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(JsonResponse.failure("图片不存在"));
            }

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("NASA图片详情查询失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("图片详情查询失败"));
        }
    }

    /**
     * 删除NASA图片记录
     * 
     * 请求示例：
     * DELETE /GHapi/nasa-daily-image/1
     * 
     * 返回示例（成功）：
     * {
     * "status": "success",
     * "message": "NASA图片删除成功"
     * }
     * 
     * 返回示例（失败）：
     * {
     * "status": "failure",
     * "message": "图片不存在"
     * }
     * 
     * @param apodId NASA图片ID
     * @return JSON格式的删除结果
     */
    @DeleteMapping("/{apodId}")
    @Operation(summary = "删除NASA图片记录")
    public ResponseEntity<JsonResponse> deleteImage(@PathVariable Long apodId) {
        try {
            if (apodId == null || apodId <= 0) {
                log.warn("图片ID无效: {}", apodId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(JsonResponse.failure("图片ID无效"));
            }

            log.info("删除NASA图片 - ID: {}", apodId);
            boolean success = nasaDailyImageService.deleteById(apodId);

            if (success) {
                return ResponseEntity.ok(JsonResponse.success("NASA图片删除成功"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(JsonResponse.failure("图片不存在"));
            }
        } catch (Exception ex) {
            log.error("NASA图片删除失败", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(JsonResponse.failure("NASA图片删除失败"));
        }
    }
}
