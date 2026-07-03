package LDS.Person.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek 自定义参数聊天请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatCustomRequest {
    
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
    
    /**
     * 采样温度 (0-2)
     * 可选，不填用默认值1.0
     */
    @JsonProperty("温度")
    private Double temperature;
    
    /**
     * 最大生成tokens
     * 可选，不填用默认值4096
     */
    @JsonProperty("最大生成tokens")
    private Integer maxTokens;
}
