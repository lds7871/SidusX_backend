package LDS.Person.controller;

import LDS.Person.dto.request.DeepSeekChatCustomRequest;
import LDS.Person.dto.request.DeepSeekChatRequest;
import LDS.Person.dto.response.DeepSeekChatResponse;
import LDS.Person.util.DeepSeekApiClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DeepSeek API 控制层
 * 提供调用DeepSeek AI模型的REST接口
 */
@RestController
@RequestMapping("/GHapi/deepseek")
@Tag(name = "DeepSeek AI", description = "调用DeepSeek聊天补全API")
//@CrossOrigin(origins = "*", maxAge = 3600)
public class DeepSeekController {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekController.class);
    
    private final DeepSeekApiClient deepSeekApiClient;
    
    public DeepSeekController() {
        this.deepSeekApiClient = new DeepSeekApiClient();
    }
    
    /**
     * 使用默认参数调用DeepSeek API
     * 
     * @param request 包含用户问题和系统提示词的请求
     * @return 包含AI回复的响应
     */
    @PostMapping("/chat")
    @Operation(summary = "聊天补全（默认参数）", 
               description = "使用默认参数（temperature=1.0, max_tokens=4096）调用DeepSeek API")
    public ResponseEntity<DeepSeekChatResponse> chatWithDefault(
            @RequestBody DeepSeekChatRequest request) {
        
        //log.info("收到DeepSeek聊天请求，问题: {}", request.getUserQuestion());
        
        try {
            // 验证请求参数
            if (request.getUserQuestion() == null || request.getUserQuestion().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    DeepSeekChatResponse.builder()
                            .success(false)
                            .message("用户问题不能为空")
                            .errorMessage("user_question is required")
                            .timestamp(System.currentTimeMillis())
                            .build()
                );
            }
            
            if (request.getSystemPrompt() == null || request.getSystemPrompt().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    DeepSeekChatResponse.builder()
                            .success(false)
                            .message("系统提示词不能为空")
                            .errorMessage("system_prompt is required")
                            .timestamp(System.currentTimeMillis())
                            .build()
                );
            }
            
            // 调用DeepSeek API
            String aiResponse = deepSeekApiClient.chatWithDefault(
                    request.getUserQuestion(), 
                    request.getSystemPrompt()
            );
            
            //log.info("DeepSeek API调用成功");
            
            return ResponseEntity.ok(
                DeepSeekChatResponse.builder()
                        .success(true)
                        .message("调用成功")
                        .aiResponse(aiResponse)
                        .timestamp(System.currentTimeMillis())
                        .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                DeepSeekChatResponse.builder()
                        .success(false)
                        .message("参数错误")
                        .errorMessage(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build()
            );
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            return ResponseEntity.internalServerError().body(
                DeepSeekChatResponse.builder()
                        .success(false)
                        .message("调用失败")
                        .errorMessage(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build()
            );
        }
    }
    
    /**
     * 使用自定义参数调用DeepSeek API
     * 
     * @param request 包含用户问题、系统提示词、温度和token数的请求
     * @return 包含AI回复的响应
     */
    @PostMapping("/chat-custom")
    @Operation(summary = "聊天补全（自定义参数）", 
               description = "使用自定义参数（temperature, max_tokens）调用DeepSeek API")
    public ResponseEntity<DeepSeekChatResponse> chatWithCustomParams(
            @RequestBody DeepSeekChatCustomRequest request) {
        
        //log.info("收到DeepSeek自定义参数聊天请求，问题: {}, 温度: {}, tokens: {}", 
         //       request.getUserQuestion(), request.getTemperature(), request.getMaxTokens());
        
        try {
            // 验证请求参数
            if (request.getUserQuestion() == null || request.getUserQuestion().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    DeepSeekChatResponse.builder()
                            .success(false)
                            .message("用户问题不能为空")
                            .errorMessage("user_question is required")
                            .timestamp(System.currentTimeMillis())
                            .build()
                );
            }
            
            if (request.getSystemPrompt() == null || request.getSystemPrompt().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    DeepSeekChatResponse.builder()
                            .success(false)
                            .message("系统提示词不能为空")
                            .errorMessage("system_prompt is required")
                            .timestamp(System.currentTimeMillis())
                            .build()
                );
            }
            
            // 验证temperature参数范围
            if (request.getTemperature() != null) {
                if (request.getTemperature() < 0 || request.getTemperature() > 2) {
                    return ResponseEntity.badRequest().body(
                        DeepSeekChatResponse.builder()
                                .success(false)
                                .message("参数错误")
                                .errorMessage("temperature必须在0-2之间")
                                .timestamp(System.currentTimeMillis())
                                .build()
                    );
                }
            }
            
            // 调用DeepSeek API
            String aiResponse = deepSeekApiClient.chatWithCustomParams(
                    request.getUserQuestion(), 
                    request.getSystemPrompt(),
                    request.getTemperature(),
                    request.getMaxTokens()
            );
            
            //log.info("DeepSeek API调用成功");
            
            return ResponseEntity.ok(
                DeepSeekChatResponse.builder()
                        .success(true)
                        .message("调用成功")
                        .aiResponse(aiResponse)
                        .timestamp(System.currentTimeMillis())
                        .build()
            );
            
        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                DeepSeekChatResponse.builder()
                        .success(false)
                        .message("参数错误")
                        .errorMessage(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build()
            );
        } catch (Exception e) {
            log.error("调用DeepSeek API失败", e);
            return ResponseEntity.internalServerError().body(
                DeepSeekChatResponse.builder()
                        .success(false)
                        .message("调用失败")
                        .errorMessage(e.getMessage())
                        .timestamp(System.currentTimeMillis())
                        .build()
            );
        }
    }
}
