package LDS.Person.repository;

import LDS.Person.entity.NasaDailyImage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * NASA APOD 每日图片信息表 Mapper 接口
 */
@Mapper
public interface NasaDailyImageMapper {

    @Insert("INSERT INTO nasa_daily_image (copyright, explanation, media_type, title, url, create_time) " +
            "VALUES (#{copyright}, #{explanation}, #{mediaType}, #{title}, #{url}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "apodId", keyColumn = "apod_id")
    int insert(NasaDailyImage nasaDailyImage);

    @Select("SELECT apod_id, copyright, explanation, media_type, title, url, create_time " +
            "FROM nasa_daily_image WHERE apod_id = #{apodId}")
    @Results({
        @Result(property = "apodId", column = "apod_id"),
        @Result(property = "mediaType", column = "media_type"),
        @Result(property = "createTime", column = "create_time")
    })
    NasaDailyImage selectById(Long apodId);

    @Select("SELECT apod_id, copyright, explanation, media_type, title, url, create_time " +
            "FROM nasa_daily_image ORDER BY create_time DESC")
    @Results({
        @Result(property = "apodId", column = "apod_id"),
        @Result(property = "mediaType", column = "media_type"),
        @Result(property = "createTime", column = "create_time")
    })
    List<NasaDailyImage> selectAll();

    @Delete("DELETE FROM nasa_daily_image WHERE apod_id = #{apodId}")
    int deleteById(Long apodId);
}
