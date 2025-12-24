package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wiki 新增请求 DTO
 * 用于接收前端创建新 Wiki 的请求
 * 
 * 说明：create_user 和 update_user 为可选字段
 * 如果不提供，则默认设置为 "system"
 */
public class WikiCreateRequest {
    
    /**
     * Wiki 键名（唯一）
     */
    @JsonProperty("key_name")
    private String keyName;
    
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
     * 创建用户（可选，为空默认 "system"）
     */
    @JsonProperty("create_user")
    private String createUser;
    
    /**
     * 更新用户（可选，为空默认 "system"）
     */
    @JsonProperty("update_user")
    private String updateUser;

    // 构造函数
    public WikiCreateRequest() {
    }

    public WikiCreateRequest(String keyName, String texts, String[] tags) {
        this.keyName = keyName;
        this.texts = texts;
        this.tags = tags;
    }

    public WikiCreateRequest(String keyName, String texts, String[] tags, String createUser, String updateUser) {
        this.keyName = keyName;
        this.texts = texts;
        this.tags = tags;
        this.createUser = createUser;
        this.updateUser = updateUser;
    }

    // Getter 和 Setter
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

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }
}
