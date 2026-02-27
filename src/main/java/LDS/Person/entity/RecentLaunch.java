package LDS.Person.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 最近一次发射数据实体类
 * 对应数据库表 recent_launch
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecentLaunch {

  /**
   * 自增主键
   */
  private Long id;

  /**
   * JSONB 格式存储的完整发射数据（Java 侧以 String 存储）
   */
  private String data;

  /**
   * 数据获取时间
   */
  private LocalDateTime getTime;
}
