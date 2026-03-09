package LDS.Person.service;

import LDS.Person.dto.response.AnnouncementResponse;
import java.util.List;

/**
 * 系统公告服务接口
 */
public interface AnnouncementService {

    /**
     * 获取最新一条公告
     *
     * @return 最新公告响应
     */
    AnnouncementResponse getLatestAnnouncement();

    /**
     * 获取最近五条公告
     *
     * @return 公告响应列表
     */
    List<AnnouncementResponse> getRecentAnnouncements();

    /**
     * 添加新公告
     *
     * @param content 公告内容
     * @return 是否添加成功
     */
    boolean addAnnouncement(String content);
}
