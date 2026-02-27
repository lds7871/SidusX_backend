package LDS.Person.tasks;

import LDS.Person.service.RecentLaunchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 最近发射数据 定时任务
 * 每天凌晨 02:10 和下午 14:10 各执行一次
 */
@Component
public class RecentLaunchTask {

  private static final Logger logger = LoggerFactory.getLogger(RecentLaunchTask.class);

  @Autowired
  private RecentLaunchService recentLaunchService;

  /**
   * 每天凌晨 02:10 执行
   * cron: 秒 分 时 日 月 周
   */
  @Scheduled(cron = "0 10 2 * * ?")
  public void fetchAtNight() {
    logger.info("===== [最近发射数据任务] 凌晨 02:10 触发 =====");
    recentLaunchService.fetchAndSaveRecentLaunch();
    logger.info("===== [最近发射数据任务] 凌晨 02:10 执行完毕 =====");
  }

  /**
   * 每天下午 14:10 执行
   * cron: 秒 分 时 日 月 周
   */
  @Scheduled(cron = "0 10 14 * * ?")
  public void fetchAtAfternoon() {
    logger.info("===== [最近发射数据任务] 下午 14:10 触发 =====");
    recentLaunchService.fetchAndSaveRecentLaunch();
    logger.info("===== [最近发射数据任务] 下午 14:10 执行完毕 =====");
  }
}
