package LDS.Person.service;

import LDS.Person.dto.response.AnnouncementResponse;
import java.util.List;

public interface AnnouncementService {
  AnnouncementResponse getLatestAnnouncement();

  List<AnnouncementResponse> getRecentAnnouncements();

  /**
   * 添加新公告
   * 
   * @param content 公告内容
   * @return 是否添加成功
   */
  boolean addAnnouncement(String content);
}
