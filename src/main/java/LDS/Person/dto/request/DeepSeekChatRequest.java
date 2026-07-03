package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek 聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatRequest {
    
    /**
     * 用户提问内容（必填）
     */
    @JsonProperty("问题")
    private String userQuestion;
    
    /**
     * 系统提示词（必填）
     */
    @JsonProperty("提示词")
    private String systemPrompt;
}
