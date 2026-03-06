package LDS.Person.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Wiki留言响应 DTO
 * 包含留言信息和用户信息
 */
@Getter
@Setter
@Schema(description = "Wiki留言响应信息")
public class WikiCommentResponse {

  @JsonProperty("reply_id")
  @Schema(description = "留言ID", example = "1")
  private Long replyId;

  @JsonProperty("wiki_id")
  @Schema(description = "对应百科ID", example = "1")
  private Long wikiId;

  @JsonProperty("user_id")
  @Schema(description = "留言用户ID", example = "1")
  private Long userId;

  @JsonProperty("text")
  @Schema(description = "留言内容")
  private String text;

  @JsonProperty("likes")
  @Schema(description = "点赞数量", example = "0")
  private Integer likes;

  @JsonProperty("create_time")
  @Schema(description = "留言时间")
  private String createTime;

  @JsonProperty("user_name")
  @Schema(description = "用户姓名")
  private String userName;

  @JsonProperty("user_cover")
  @Schema(description = "用户头像（Base64编码格式）")
  private String userCover;

  // 构造函数
  public WikiCommentResponse() {
  }

  public WikiCommentResponse(Long replyId, Long wikiId, Long userId, String text, Integer likes,
      String createTime, String userName, String userCover) {
    this.replyId = replyId;
    this.wikiId = wikiId;
    this.userId = userId;
    this.text = text;
    this.likes = likes;
    this.createTime = createTime;
    this.userName = userName;
    this.userCover = userCover;
  }
}
