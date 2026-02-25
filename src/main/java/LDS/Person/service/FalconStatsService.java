package LDS.Person.service;

import LDS.Person.entity.FalconStats;

import java.util.List;

/**
 * SpaceX Falcon 火箭统计数据服务接口
 */
public interface FalconStatsService {

  /**
   * 获取并保存Falcon统计数据
   */
  void fetchAndSaveFalconStats();

  /**
   * 保存Falcon统计数据
   *
   * @param falconStats Falcon统计数据
   * @return 是否保存成功
   */
  boolean save(FalconStats falconStats);

  /**
   * 根据ID查询Falcon统计数据
   *
   * @param falconId 数据ID
   * @return Falcon统计数据
   */
  FalconStats getById(Long falconId);

  /**
   * 查询所有Falcon统计数据
   *
   * @return Falcon统计数据列表
   */
  List<FalconStats> list();
}
