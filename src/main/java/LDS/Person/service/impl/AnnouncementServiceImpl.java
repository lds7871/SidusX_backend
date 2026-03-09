package LDS.Person.service.impl;

import LDS.Person.dto.response.AnnouncementResponse;
import LDS.Person.entity.Announcement;
import LDS.Person.repository.AnnouncementMapper;
import LDS.Person.service.AnnouncementService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

  private final AnnouncementMapper announcementMapper;

  public AnnouncementServiceImpl(AnnouncementMapper announcementMapper) {
    this.announcementMapper = announcementMapper;
  }

  @Override
  public AnnouncementResponse getLatestAnnouncement() {
    Announcement announcement = announcementMapper.findLatest();
    if (announcement == null) {
      return null;
    }
    AnnouncementResponse response = new AnnouncementResponse();
    BeanUtils.copyProperties(announcement, response);
    return response;
  }

  @Override
  public List<AnnouncementResponse> getRecentAnnouncements() {
    List<Announcement> announcements = announcementMapper.findRecentFive();
    return announcements.stream().map(ann -> {
      AnnouncementResponse response = new AnnouncementResponse();
      BeanUtils.copyProperties(ann, response);
      return response;
    }).collect(Collectors.toList());
  }

  @Override
  public boolean addAnnouncement(String content) {
    Announcement announcement = new Announcement();
    announcement.setContent(content);
    announcement.setCreateTime(LocalDateTime.now());
    return announcementMapper.insert(announcement) > 0;
  }
}
