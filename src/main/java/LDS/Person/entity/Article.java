package LDS.Person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类
 */
@Data
@TableName("article")
@Schema(description = "文章信息")
public class Article {

    @TableId(type = IdType.AUTO)
    @Schema(description = "文章ID，自增主键", example = "1")
    private Long articleId;

    @Schema(description = "文章标题", example = "技术分享")
    private String title;

    @Schema(description = "封面图片地址", example = "https://example.com/cover.jpg")
    private String cover;

    @Schema(description = "文章简介", example = "这是一篇技术分享文章")
    private String info;

    @Schema(description = "文章正文内容")
    private String texts;

    @Schema(description = "文章标签（逗号分隔）", example = "Java,Spring,技术")
    private String tags;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
