package LDS.Person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 访问日志实体类
 */
@Data
@TableName("api_log")
@Schema(description = "访问日志信息")
public class ApiLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "访问者IP地址")
    private String ip;

    @Schema(description = "访问路径")
    private String api;

    @Schema(description = "状态")
    private Integer states;

    @Schema(description = "访问时间")
    @TableField("create_time")
    private LocalDateTime createTime;
}
