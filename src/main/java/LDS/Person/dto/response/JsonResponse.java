package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 通用 JSON 响应 DTO
 * 用于删除等简单操作的响应
 */
public class JsonResponse {
    
    /**
     * 响应消息
     */
    @JsonProperty("message")
    private String message;
    
    /**
     * 是否成功
     */
    @JsonProperty("success")
    private Boolean success;
    
    /**
     * 响应数据
     */
    @JsonProperty("data")
    private Object data;

    // 构造函数
    public JsonResponse() {
    }

    public JsonResponse(String message, Boolean success) {
        this.message = message;
        this.success = success;
    }

    public JsonResponse(String message, Boolean success, Object data) {
        this.message = message;
        this.success = success;
        this.data = data;
    }

    // 静态工厂方法
    public static JsonResponse success(String message) {
        return new JsonResponse(message, true);
    }

    public static JsonResponse success(String message, Object data) {
        return new JsonResponse(message, true, data);
    }

    public static JsonResponse failure(String message) {
        return new JsonResponse(message, false);
    }

    public static JsonResponse failure(String message, Object data) {
        return new JsonResponse(message, false, data);
    }

    // Getter 和 Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
