package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 分页响应包装类
 * 用于统一返回分页数据
 */
public class PageResponse<T> {
    
    /**
     * 当前页码
     */
    @JsonProperty("page")
    private Integer page;
    
    /**
     * 每页数量
     */
    @JsonProperty("page_size")
    private Integer pageSize;
    
    /**
     * 总记录数
     */
    @JsonProperty("total_count")
    private Long totalCount;
    
    /**
     * 总页数
     */
    @JsonProperty("total_pages")
    private Long totalPages;
    
    /**
     * 分页数据列表
     */
    @JsonProperty("data")
    private List<T> data;

    // 构造函数
    public PageResponse() {
    }

    public PageResponse(Integer page, Integer pageSize, Long totalCount, Long totalPages, List<T> data) {
        this.page = page;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.data = data;
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

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
