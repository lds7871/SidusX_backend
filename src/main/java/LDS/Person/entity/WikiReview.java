package LDS.Person.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Wiki 审核内容实体类
 */
@Data
@Schema(description = "Wiki 审核内容实体")
public class WikiReview {

    @Schema(description = "Wiki 审核 ID")
    private Long wikireviewId;

    @Schema(description = "关联的 Wiki ID")
    private Long wikiId;

    @Schema(description = "Wiki 审核内容文本")
    private String texts;

    @Schema(description = "标签数组")
    private String[] tags;

    @Schema(description = "版本号")
    private Double version;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "更新用户 ID")
    private String updateUser;

    @Schema(description = "审核状态：0待审核，1通过，2拒绝")
    private Integer wikiStates;
}
