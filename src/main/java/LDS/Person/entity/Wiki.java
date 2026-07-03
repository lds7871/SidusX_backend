package LDS.Person.entity;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Wiki 内容实体
 * 用于存储Wiki条目信息
 */
public class Wiki {
    
    /**
     * Wiki ID，自增主键
     */
    private Long wikiId;
    
    /**
     * Wiki 键名（唯一）
     */
    private String keyName;
    
    /**
     * Wiki 内容文本
     */
    private String texts;
    
    /**
     * 标签数组
     */
    private String[] tags;
    
    /**
     * 版本号
     */
    private Double version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 创建用户
     */
    private String createUser;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 更新用户
     */
    private String updateUser;

    // 构造函数
    public Wiki() {
    }

    public Wiki(Long wikiId, String keyName, String texts, String[] tags, Double version,
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
    }

    // Getter 和 Setter
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

    @Override
    public String toString() {
        return "Wiki{" +
                "wikiId=" + wikiId +
                ", keyName='" + keyName + '\'' +
                ", texts='" + texts + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", version=" + version +
                ", createTime=" + createTime +
                ", createUser='" + createUser + '\'' +
                ", updateTime=" + updateTime +
                ", updateUser='" + updateUser + '\'' +
                '}';
    }
}
