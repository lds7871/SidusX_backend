package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wiki 更新请求 DTO
 * 用于接收前端更新 Wiki 的请求
 * 
 * 说明：update_time 默认为当前时间，其它字段需手动输入
 */
public class WikiUpdateRequest {

  /**
   * Wiki 内容文本
   */
  @JsonProperty("texts")
  private String texts;

  /**
   * 标签数组
   */
  @JsonProperty("tags")
  private String[] tags;

  /**
   * 版本号
   */
  @JsonProperty("version")
  private Double version;

  /**
   * 更新用户（必填）
   */
  @JsonProperty("update_user")
  private String updateUser;

  // 构造函数
  public WikiUpdateRequest() {
  }

  public WikiUpdateRequest(String texts, String[] tags, Double version, String updateUser) {
    this.texts = texts;
    this.tags = tags;
    this.version = version;
    this.updateUser = updateUser;
  }

  // Getter 和 Setter
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

  public String getUpdateUser() {
    return updateUser;
  }

  public void setUpdateUser(String updateUser) {
    this.updateUser = updateUser;
  }

  @Override
  public String toString() {
    return "WikiUpdateRequest{" +
        "texts='" + texts + '\'' +
        ", tags=" + java.util.Arrays.toString(tags) +
        ", version=" + version +
        ", updateUser='" + updateUser + '\'' +
        '}';
  }
}
