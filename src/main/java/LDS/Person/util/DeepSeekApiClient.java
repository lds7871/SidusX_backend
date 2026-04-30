package LDS.Person.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek API 客户端
 * 用于调用DeepSeek聊天补全API
 * API文档: https://api-docs.deepseek.com/zh-cn/api/create-chat-completion
 * 
 * 使用示例:
 * DeepSeekApiClient client = new DeepSeekApiClient();
 * 
 * String response = client.chatWithDefault("问题","提示词");
 * 
 * String response2 = client.chatWithCustomParams("问题","提示词",0.7,1000);
 */
public class DeepSeekApiClient {

    private static final Logger logger = LoggerFactory.getLogger(DeepSeekApiClient.class);

    private static final String API_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL = "deepseek-v4-flash";
    private static final int DEFAULT_MAX_TOKENS = 4096;
    private static final double DEFAULT_TEMPERATURE = 1.0;
    private static final double DEFAULT_TOP_P = 1.0;

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * 构造函数 - 从环境变量读取API Key
     * 
     * @throws IllegalArgumentException 如果环境变量DEEPSEEK_API_KEY未设置
     */
    public DeepSeekApiClient() {
        this.apiKey = getApiKeyFromEnv();
        this.httpClient = LDS.Person.config.HttpClientFactory.getInstance();
        this.objectMapper = new ObjectMapper();

        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("DEEPSEEK_API_KEY 环境变量未设置");
        }
    }

    /**
     * 从环境变量读取API Key
     * 
     * @return API Key 字符串
     */
    private String getApiKeyFromEnv() {
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        if (apiKey == null) {
            logger.warn("环境变量 DEEPSEEK_API_KEY 未设置");
        }
        return apiKey;
    }

    /**
     * 调用DeepSeek API进行聊天补全
     * 
     * @param userQuestion 用户提问内容
     * @param systemPrompt 系统提示词
     * @return API返回的回复内容
     * @throws Exception 如果API调用失败
     */
    public String chatWithDefault(String userQuestion, String systemPrompt) throws Exception {
        if (userQuestion == null || userQuestion.isEmpty()) {
            throw new IllegalArgumentException("用户问题不能为空");
        }

        // if (systemPrompt == null || systemPrompt.isEmpty()) {
        // throw new IllegalArgumentException("系统提示词不能为空");
        // }

        // 构建请求体
        ChatRequest request = buildChatRequest(userQuestion, systemPrompt);

        // 转换为JSON
        String requestBody = objectMapper.writeValueAsString(request);
        logger.debug("调用DeepSeekAPI-->chatWithDefault");

        // 发送HTTP请求
        HttpRequest httpRequest = buildHttpRequest(requestBody);
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        // 解析响应
        return parseResponse(response.body());
    }

    /**
     * 高级版本 - 支持自定义参数
     * 
     * @param userQuestion 用户提问内容
     * @param systemPrompt 系统提示词
     * @param temperature  采样温度 (0-2)，默认1.0
     * @param maxTokens    最大生成tokens，默认4096
     * @return API返回的回复内容
     * @throws Exception 如果API调用失败
     */
    public String chatWithCustomParams(String userQuestion, String systemPrompt,
            Double temperature, Integer maxTokens) throws Exception {
        if (userQuestion == null || userQuestion.isEmpty()) {
            throw new IllegalArgumentException("用户问题不能为空");
        }

        // if (systemPrompt == null || systemPrompt.isEmpty()) {
        // throw new IllegalArgumentException("系统提示词不能为空");
        // }

        // 构建请求体
        ChatRequest request = buildChatRequest(userQuestion, systemPrompt);

        // 设置自定义参数
        if (temperature != null) {
            request.setTemperature(temperature);
        }
        if (maxTokens != null) {
            request.setMax_tokens(maxTokens);
        }

        // 转换为JSON
        String requestBody = objectMapper.writeValueAsString(request);
        logger.debug("调用DeepSeekAPI-->chatWithCustomParams");

        // 发送HTTP请求
        HttpRequest httpRequest = buildHttpRequest(requestBody);
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        // 解析响应
        return parseResponse(response.body());
    }

    /**
     * 构建聊天请求对象
     */
    private ChatRequest buildChatRequest(String userQuestion, String systemPrompt) {
        ChatRequest request = new ChatRequest();
        request.setModel(MODEL);
        request.setTemperature(DEFAULT_TEMPERATURE);
        request.setMax_tokens(DEFAULT_MAX_TOKENS);
        request.setTop_p(DEFAULT_TOP_P);
        request.setStream(false);

        // 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 添加系统提示词
        messages.add(new Message("system", systemPrompt));

        // 添加用户问题
        messages.add(new Message("user", userQuestion));

        request.setMessages(messages);

        return request;
    }

    /**
     * 构建HTTP请求
     */
    private HttpRequest buildHttpRequest(String requestBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 解析API响应
     */
    private String parseResponse(String responseBody) throws Exception {
        logger.debug("接收到DeepSeekAPI响应");

        ChatResponse response = objectMapper.readValue(responseBody, ChatResponse.class);

        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
            Choice choice = response.getChoices().get(0);
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                return choice.getMessage().getContent();
            }
        }

        throw new RuntimeException("API响应格式错误或无有效内容");
    }

    // ==================== 内部数据类 ====================

    /**
     * 聊天请求体
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ChatRequest {
        private String model;
        private List<Message> messages;
        private Double temperature;
        @JsonProperty("max_tokens")
        private Integer max_tokens;
        @JsonProperty("top_p")
        private Double top_p;
        private Boolean stream;
    }

    /**
     * 消息对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Message {
        private String role; // system, user, assistant
        private String content;
    }

    /**
     * API响应体
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatResponse {
        private String id;
        private List<Choice> choices;
        private Long created;
        private String model;
        private Usage usage;
    }

    /**
     * 选择对象 (choices)
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        private Message message;
        private String finish_reason;
        private Integer index;
    }

    /**
     * token使用情况
     */
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer prompt_tokens;
        @JsonProperty("completion_tokens")
        private Integer completion_tokens;
        @JsonProperty("total_tokens")
        private Integer total_tokens;
    }

    // ==================== Main测试方法 ====================

    /**
     * 简单的测试main方法
     * 使用示例：
     * System.setProperty("DEEPSEEK_API_KEY", "sk-xxxxx");
     */
    public static void main(String[] args) throws Exception {
        // 初始化客户端（会从环境变量读取API Key）
        DeepSeekApiClient client = new DeepSeekApiClient();

        // 示例1: 基本用法
        String question = "请解释什么是机器学习";
        String systemPrompt = "你是一个友好的AI助手，请用简洁的语言解释概念";

        try {
            String response = client.chatWithDefault(question, systemPrompt);
            System.out.println("用户提问: " + question);
            System.out.println("系统提示: " + systemPrompt);
            System.out.println("AI回复: " + response);

        } catch (Exception e) {
            System.err.println("调用失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
