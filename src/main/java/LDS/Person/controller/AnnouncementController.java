package LDS.Person.controller;

import LDS.Person.config.BypassIpWhitelist;
import LDS.Person.dto.response.AnnouncementResponse;
import LDS.Person.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/GHapi/announcement")
@Tag(name = "系统公告接口", description = "公告查询相关接口")
public class AnnouncementController {

  private final AnnouncementService announcementService;

  public AnnouncementController(AnnouncementService announcementService) {
    this.announcementService = announcementService;
  }

  @BypassIpWhitelist
  @GetMapping("/latest")
  @Operation(summary = "显示最新的一条公告")
  public AnnouncementResponse getLatest() {
    return announcementService.getLatestAnnouncement();
  }

  @BypassIpWhitelist
  @GetMapping("/recent")
  @Operation(summary = "显示最近五条公告")
  public List<AnnouncementResponse> getRecent() {
    return announcementService.getRecentAnnouncements();
  }
}
