package LDS.Person.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SpaceX Falcon 系列火箭统计数据表实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FalconStats {

  /**
   * 自增主键
   */
  private Long falconId;

  /**
   * SpaceX 文档 ID
   */
  private String documentId;

  /**
   * 总发射次数
   */
  private Integer totalLaunches;

  /**
   * 总着陆次数
   */
  private Integer totalLandings;

  /**
   * 总复用次数
   */
  private Integer totalReflights;

  /**
   * 数据写入数据库的本地时间
   */
  private LocalDateTime createdAt;
}
