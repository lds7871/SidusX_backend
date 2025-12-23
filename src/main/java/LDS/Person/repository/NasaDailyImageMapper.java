package LDS.Person.repository;

import LDS.Person.entity.NasaDailyImage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * NASA APOD 每日图片信息表 Mapper 接口
 */
@Mapper
public interface NasaDailyImageMapper extends BaseMapper<NasaDailyImage> {
}
