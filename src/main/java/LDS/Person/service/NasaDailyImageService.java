package LDS.Person.service;

import LDS.Person.entity.NasaDailyImage;

import java.util.List;

/**
 * NASA APOD 每日图片信息服务接口
 */
public interface NasaDailyImageService {
    
    /**
     * 获取并保存今日NASA APOD图片
     */
    void fetchAndSaveApodImage();

    /**
     * 保存NASA图片信息
     *
     * @param nasaDailyImage NASA图片信息
     * @return 是否保存成功
     */
    boolean save(NasaDailyImage nasaDailyImage);

    /**
     * 根据ID查询NASA图片信息
     *
     * @param apodId 图片ID
     * @return NASA图片信息
     */
    NasaDailyImage getById(Long apodId);

    /**
     * 查询所有NASA图片信息
     *
     * @return NASA图片信息列表
     */
    List<NasaDailyImage> list();
}
