package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wiki 分页查询请求 DTO
 * 包含分页参数和多条件查询过滤
 */
public class WikiPageQueryRequest {
    
    /**
     * 当前页码（从 1 开始）
     */
    @JsonProperty("page")
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    @JsonProperty("page_size")
    private Integer pageSize = 10;
    
    /**
     * Wiki 键名（模糊匹配）
     */
    @JsonProperty("key_name")
    private String keyName;
    
    /**
     * 标签（模糊匹配，逗号分隔）
     */
    @JsonProperty("tags")
    private String tags;
    
    /**
     * 创建时间范围 - 开始时间（ISO 8601 格式：yyyy-MM-dd HH:mm:ss）
     */
    @JsonProperty("create_time_start")
    private String createTimeStart;
    
    /**
     * 创建时间范围 - 结束时间（ISO 8601 格式：yyyy-MM-dd HH:mm:ss）
     */
    @JsonProperty("create_time_end")
    private String createTimeEnd;

    // 构造函数
    public WikiPageQueryRequest() {
    }

    public WikiPageQueryRequest(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    // Getter 和 Setter
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getCreateTimeStart() {
        return createTimeStart;
    }

    public void setCreateTimeStart(String createTimeStart) {
        this.createTimeStart = createTimeStart;
    }

    public String getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(String createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }
}
