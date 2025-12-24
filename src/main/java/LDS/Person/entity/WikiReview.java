package LDS.Person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Wiki 审核内容实体类
 */
@Data
@TableName(value = "wiki_review", autoResultMap = true)
@Schema(description = "Wiki 审核内容实体")
public class WikiReview {

    @TableId(value = "wikireview_id", type = IdType.AUTO)
    @Schema(description = "Wiki 审核 ID")
    private Long wikireviewId;

    @TableField("wiki_id")
    @Schema(description = "关联的 Wiki ID")
    private Long wikiId;

    @TableField("texts")
    @Schema(description = "Wiki 审核内容文本")
    private String texts;

    @TableField(value = "tags", typeHandler = org.apache.ibatis.type.ArrayTypeHandler.class)
    @Schema(description = "标签数组")
    private String[] tags;

    @TableField("version")
    @Schema(description = "版本号")
    private Double version;

    @TableField("update_time")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @TableField("update_user")
    @Schema(description = "更新用户 ID")
    private String updateUser;

    @TableField("wiki_states")
    @Schema(description = "审核状态：0待审核，1通过，2拒绝")
    private Integer wikiStates;
}
