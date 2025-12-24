package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import LDS.Person.entity.ApiLog;

/**
 * 访问日志分页查询响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "访问日志分页查询响应")
public class ApiLogPageResponse {
    
    @JsonProperty("status_code")
    @Schema(description = "状态码", example = "200")
    private Integer statusCode;
    
    @JsonProperty("message")
    @Schema(description = "响应消息")
    private String message;
    
    @JsonProperty("data")
    @Schema(description = "分页数据")
    private PageData pageData;
    
    @JsonProperty("timestamp")
    @Schema(description = "响应时间戳")
    private Long timestamp;
    
    /**
     * 分页数据内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分页数据信息")
    public static class PageData {
        
        @JsonProperty("current_page")
        @Schema(description = "当前页码")
        private Long currentPage;
        
        @JsonProperty("page_size")
        @Schema(description = "每页条数")
        private Long pageSize;
        
        @JsonProperty("total_count")
        @Schema(description = "总记录数")
        private Long totalCount;
        
        @JsonProperty("total_pages")
        @Schema(description = "总页数")
        private Long totalPages;
        
        @JsonProperty("records")
        @Schema(description = "日志记录列表")
        private List<ApiLog> records;
    }
    
    /**
     * 创建成功响应
     */
    public static ApiLogPageResponse success(Long currentPage, Long pageSize, Long totalCount, 
                                             Long totalPages, List<ApiLog> records) {
        ApiLogPageResponse response = new ApiLogPageResponse();
        response.setStatusCode(200);
        response.setMessage("✅ 日志查询成功");
        response.setTimestamp(System.currentTimeMillis());
        
        PageData pageData = new PageData();
        pageData.setCurrentPage(currentPage);
        pageData.setPageSize(pageSize);
        pageData.setTotalCount(totalCount);
        pageData.setTotalPages(totalPages);
        pageData.setRecords(records);
        
        response.setPageData(pageData);
        return response;
    }
    
    /**
     * 创建失败响应
     */
    public static ApiLogPageResponse error(String message) {
        ApiLogPageResponse response = new ApiLogPageResponse();
        response.setStatusCode(500);
        response.setMessage("❌ 查询失败: " + message);
        response.setTimestamp(System.currentTimeMillis());
        response.setPageData(null);
        return response;
    }
}
