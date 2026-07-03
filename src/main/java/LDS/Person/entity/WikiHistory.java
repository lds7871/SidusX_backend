package LDS.Person.entity;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Wiki 历史记录实体
 * 用于存储Wiki内容的历史备份信息
 */
public class WikiHistory {

  /**
   * 历史记录ID，自增主键
   */
  private Long historyId;

  /**
   * 来源 wiki 主表ID（外键）
   */
  private Long wikiId;

  /**
   * Wiki 键名（可重复）
   */
  private String keyName;

  /**
   * 历史Wiki内容文本
   */
  private String texts;

  /**
   * 历史标签
   */
  private String[] tags;

  /**
   * 历史版本号
   */
  private Double version;

  /**
   * 原创建时间
   */
  private LocalDateTime createTime;

  /**
   * 原创建用户
   */
  private String createUser;

  /**
   * 原更新时间
   */
  private LocalDateTime updateTime;

  /**
   * 原更新用户
   */
  private String updateUser;

  /**
   * 备份入历史表的时间
   */
  private LocalDateTime backupTime;

  // 构造函数
  public WikiHistory() {
  }

  public WikiHistory(Long wikiId, String keyName, String texts, String[] tags, Double version,
      LocalDateTime createTime, String createUser, LocalDateTime updateTime, String updateUser) {
    this.wikiId = wikiId;
    this.keyName = keyName;
    this.texts = texts;
    this.tags = tags;
    this.version = version;
    this.createTime = createTime;
    this.createUser = createUser;
    this.updateTime = updateTime;
    this.updateUser = updateUser;
    this.backupTime = LocalDateTime.now();
  }

  // Getter 和 Setter
  public Long getHistoryId() {
    return historyId;
  }

  public void setHistoryId(Long historyId) {
    this.historyId = historyId;
  }

  public Long getWikiId() {
    return wikiId;
  }

  public void setWikiId(Long wikiId) {
    this.wikiId = wikiId;
  }

  public String getKeyName() {
    return keyName;
  }

  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  public String getTexts() {
    return texts;
  }

  public void setTexts(String texts) {
    this.texts = texts;
  }

  public String[] getTags() {
    return tags;
  }

  public void setTags(String[] tags) {
    this.tags = tags;
  }

  public Double getVersion() {
    return version;
  }

  public void setVersion(Double version) {
    this.version = version;
  }

  public LocalDateTime getCreateTime() {
    return createTime;
  }

  public void setCreateTime(LocalDateTime createTime) {
    this.createTime = createTime;
  }

  public String getCreateUser() {
    return createUser;
  }

  public void setCreateUser(String createUser) {
    this.createUser = createUser;
  }

  public LocalDateTime getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(LocalDateTime updateTime) {
    this.updateTime = updateTime;
  }

  public String getUpdateUser() {
    return updateUser;
  }

  public void setUpdateUser(String updateUser) {
    this.updateUser = updateUser;
  }

  public LocalDateTime getBackupTime() {
    return backupTime;
  }

  public void setBackupTime(LocalDateTime backupTime) {
    this.backupTime = backupTime;
  }

  @Override
  public String toString() {
    return "WikiHistory{" +
        "historyId=" + historyId +
        ", wikiId=" + wikiId +
        ", keyName='" + keyName + '\'' +
        ", texts='" + texts + '\'' +
        ", tags=" + Arrays.toString(tags) +
        ", version=" + version +
        ", createTime=" + createTime +
        ", createUser='" + createUser + '\'' +
        ", updateTime=" + updateTime +
        ", updateUser='" + updateUser + '\'' +
        ", backupTime=" + backupTime +
        '}';
  }
}
