package LDS.Person.tasks;

import LDS.Person.service.FalconStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SpaceX Falcon 火箭统计数据 定时任务
 * 每天凌晨2点05分执行一次
 */
@Component
public class FalconDailyTask {

  private static final Logger logger = LoggerFactory.getLogger(FalconDailyTask.class);

  @Autowired
  private FalconStatsService falconStatsService;

  /**
   * 定时获取并保存SpaceX Falcon火箭统计数据
   * cron表达式: 0 5 2 * * ? 表示每天凌晨2点05分执行
   * 秒 分 时 日 月 周 年
   */
  @Scheduled(cron = "0 5 2 * * ?")
  public void fetchFalconStatsDaily() {
    logger.info("===== 开始执行 SpaceX Falcon 火箭统计数据定时任务 =====");
    try {
      falconStatsService.fetchAndSaveFalconStats();
      logger.info("===== SpaceX Falcon 火箭统计数据定时任务执行完成 =====");
    } catch (Exception e) {
      logger.error("SpaceX Falcon 火箭统计数据定时任务执行失败: {}", e.getMessage(), e);
    }
  }
}
