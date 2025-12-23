package LDS.Person.tasks;

import LDS.Person.service.NasaDailyImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * NASA APOD 每日图片定时任务
 * 每天凌晨2点执行一次
 */
@Component
public class NasaDailyImageTask {

    private static final Logger logger = LoggerFactory.getLogger(NasaDailyImageTask.class);

    @Autowired
    private NasaDailyImageService nasaDailyImageService;

    /**
     * 定时获取并保存NASA APOD每日图片
     * cron表达式: 0 0 2 * * ? 表示每天凌晨2点执行
     * 秒 分 时 日 月 周 年
     */
    @Scheduled(cron = "0 48 16 * * ?")//0 0 2 * * ?
    public void fetchApodImageDaily() {
        logger.info("===== 开始执行 NASA APOD 每日图片定时任务 =====");
        try {
            nasaDailyImageService.fetchAndSaveApodImage();
            logger.info("===== NASA APOD 每日图片定时任务执行完成 =====");
        } catch (Exception e) {
            logger.error("NASA APOD 每日图片定时任务执行失败: {}", e.getMessage(), e);
        }
    }
}
