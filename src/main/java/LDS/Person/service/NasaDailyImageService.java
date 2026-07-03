package LDS.Person.service;

import LDS.Person.dto.request.NasaDailyImagePageQueryRequest;
import LDS.Person.dto.response.NasaDailyImageListResponse;
import LDS.Person.dto.response.NasaDailyImageDetailResponse;
import LDS.Person.dto.response.PageResponse;
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

    /**
     * 分页查询NASA图片列表
     * 支持按标题和时间范围筛选
     * 
     * @param request 分页查询请求
     * @return 分页响应
     */
    PageResponse<NasaDailyImageListResponse> pageQuery(NasaDailyImagePageQueryRequest request);

    /**
     * 获取NASA图片详细信息
     * 
     * @param apodId 图片ID
     * @return 图片详细信息
     */
    NasaDailyImageDetailResponse getDetail(Long apodId);

    /**
     * 获取最新一条NASA图片详细信息
     *
     * @return 最新图片详细信息，若无记录则返回null
     */
    NasaDailyImageDetailResponse getLatest();

    /**
     * 删除NASA图片记录
     * 
     * @param apodId 图片ID
     * @return 是否删除成功
     */
    boolean deleteById(Long apodId);
}
