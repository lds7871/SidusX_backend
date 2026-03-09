package LDS.Person.service;

import LDS.Person.dto.response.AnnouncementResponse;
import java.util.List;

public interface AnnouncementService {
  AnnouncementResponse getLatestAnnouncement();

  List<AnnouncementResponse> getRecentAnnouncements();
}
