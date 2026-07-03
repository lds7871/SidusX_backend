package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek 聊天响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeepSeekChatResponse {
    
    /**
     * 是否成功
     */
    private Boolean success;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * AI的回复内容
     */
    @JsonProperty("回复")
    private String aiResponse;
    
    /**
     * 错误信息（仅当失败时返回）
     */
    @JsonProperty("错误信息")
    private String errorMessage;
    
    /**
     * 时间戳
     */
    private Long timestamp;
}
