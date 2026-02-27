package LDS.Person.controller;

import LDS.Person.config.BypassIpWhitelist;
import LDS.Person.dto.response.JsonResponse;
import LDS.Person.dto.response.RecentLaunchDataResponse;
import LDS.Person.service.RecentLaunchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 最近发射数据控制层
 * 提供 recent_launch 表最新数据的公开查询接口
 */
@RestController
@RequestMapping("/GHapi/recent-launch")
@Tag(name = "最近发射数据", description = "查询 recent_launch 表中的最新发射数据")
public class RecentLaunchController {

  private static final Logger log = LoggerFactory.getLogger(RecentLaunchController.class);

  private final RecentLaunchService recentLaunchService;

  public RecentLaunchController(RecentLaunchService recentLaunchService) {
    this.recentLaunchService = recentLaunchService;
  }

  /**
   * 获取最新一条发射数据的 JSONB 内容
   * 公开接口，允许任意 IP 访问
   */
  @GetMapping("/latest")
  @BypassIpWhitelist(reason = "公开接口，允许任意 IP 查询最新发射数据")
  @Operation(summary = "获取最新发射数据", description = "查询 recent_launch 表中 get_time 最新的一条记录，返回其完整 data JSONB 内容")
  public ResponseEntity<JsonResponse> getLatest() {
    log.info("收到获取最新发射数据请求");
    RecentLaunchDataResponse data = recentLaunchService.getLatestData();
    if (data == null) {
      log.warn("最新发射数据查询结果为空");
      return ResponseEntity.ok(JsonResponse.failure("暂无发射数据"));
    }
    log.info("最新发射数据查询成功，记录 ID: {}", data.getId());
    return ResponseEntity.ok(JsonResponse.success("获取成功", data));
  }
}
