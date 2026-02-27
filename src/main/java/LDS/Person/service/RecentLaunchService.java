package LDS.Person.service;

import LDS.Person.dto.response.RecentLaunchDataResponse;

/**
 * 最近发射数据服务接口
 */
public interface RecentLaunchService {

  /**
   * 从 API 获取最近一次发射数据并保存到数据库
   * 同时维护表内记录不超过 5 条，只保留最新的五条
   */
  void fetchAndSaveRecentLaunch();

  /**
   * 获取表内最新一条发射数据的 JSONB 内容
   *
   * @return 最新发射数据响应 DTO，无记录时返回 null
   */
  RecentLaunchDataResponse getLatestData();
}
