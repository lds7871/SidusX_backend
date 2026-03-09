package LDS.Person.repository;

import LDS.Person.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface AnnouncementMapper {

  @Select("SELECT ann_id as annId, content, create_time as createTime FROM announcement ORDER BY create_time DESC LIMIT 1")
  Announcement findLatest();

  @Select("SELECT ann_id as annId, content, create_time as createTime FROM announcement ORDER BY create_time DESC LIMIT 5")
  List<Announcement> findRecentFive();
}
