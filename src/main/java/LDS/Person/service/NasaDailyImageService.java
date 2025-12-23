package LDS.Person.service;

import LDS.Person.entity.NasaDailyImage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * NASA APOD 每日图片信息服务接口
 */
public interface NasaDailyImageService extends IService<NasaDailyImage> {
    
    /**
     * 获取并保存今日NASA APOD图片
     */
    void fetchAndSaveApodImage();
}
