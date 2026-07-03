package LDS.Person.controller;

import LDS.Person.service.MsShipService;
import LDS.Person.dto.request.MsShipCreateRequest;
import LDS.Person.dto.response.MsShipResponse;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.config.BypassIpWhitelist;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * MS_SHIP 控制层
 * 提供 MS_SHIP 的创建和查询接口
 */
@RestController
@RequestMapping("/GHapi/ms-ship")
@Tag(name = "MS_SHIP 管理", description = "MS_SHIP 数据管理接口")
public class MsShipController {

  private static final Logger log = LoggerFactory.getLogger(MsShipController.class);

  private final MsShipService msShipService;

  public MsShipController(MsShipService msShipService) {
    this.msShipService = msShipService;
  }

  /**
   * 创建 MS_SHIP 记录
   *
   * 请求体示例：
   * {
   * "content": "{\"name\": \"Missile Ship\", \"type\": \"submarine\",
   * \"armament\": [\"missiles\", \"torpedoes\"]}"
   * }
   *
   * 说明：
   * - content 为 JSON 格式的数据，可以是对象或数组
   * - msId 自动生成，由数据库自增
   *
   * @param request 创建请求
   * @return 创建成功的 MS_SHIP 响应
   */
  @PostMapping("/create")
  @BypassIpWhitelist(reason = "MS_SHIP 创建接口")
  @Operation(summary = "创建新的 MS_SHIP 记录")
  public ResponseEntity<?> createMsShip(
      @org.springframework.web.bind.annotation.RequestBody MsShipCreateRequest request) {
    try {
      log.info("创建 MS_SHIP 记录");
      MsShipResponse response = msShipService.createMsShip(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (IllegalArgumentException ex) {
      log.warn("MS_SHIP 创建参数验证失败: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(JsonResponse.failure(ex.getMessage()));
    } catch (Exception ex) {
      log.error("MS_SHIP 创建失败", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(JsonResponse.failure("MS_SHIP 创建失败: " + ex.getMessage()));
    }
  }

  /**
   * 根据 MS_ID 查询 MS_SHIP 记录
   *
   * @param msId MS_SHIP 记录ID
   * @return MS_SHIP 响应
   */
  @GetMapping("/{msId}")
  @BypassIpWhitelist(reason = "MS_SHIP 查询接口")
  @Operation(summary = "根据 MS_ID 查询 MS_SHIP 记录")
  public ResponseEntity<?> getMsShipById(@PathVariable Long msId) {
    try {
      log.info("查询 MS_SHIP 记录 - msId: {}", msId);
      MsShipResponse response = msShipService.getMsShipById(msId);
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException ex) {
      log.warn("MS_SHIP 查询参数验证失败: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(JsonResponse.failure(ex.getMessage()));
    } catch (Exception ex) {
      log.error("MS_SHIP 查询失败 - msId: {}", msId, ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(JsonResponse.failure("MS_SHIP 查询失败: " + ex.getMessage()));
    }
  }
}
