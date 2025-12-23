package LDS.Person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NASA APOD 每日图片信息表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("nasa_daily_image")
public class NasaDailyImage {

    /**
     * 图片记录ID，自增主键
     */
    @TableId(value = "apod_id", type = IdType.AUTO)
    private Long apodId;

    /**
     * 图片大标题
     */
    private String copyright;

    /**
     * 图片说明文字（翻译为中文）
     */
    private String explanation;

    /**
     * 媒体类型（image 或 video）
     */
    private String mediaType;

    /**
     * 每日图片标题
     */
    private String title;

    /**
     * 图片或视频的访问链接
     */
    private String url;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;
}
