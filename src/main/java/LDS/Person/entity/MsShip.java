package LDS.Person.entity;

/**
 * MS_SHIP表实体类
 * 存储完整百科 JSON 内容
 */
public class MsShip {

  /** MS_SHIP记录ID，自增主键 */
  private Long msId;

  /** 存储完整百科 JSON 内容 */
  private String content;

  public MsShip() {
  }

  public MsShip(String content) {
    this.content = content;
  }

  public MsShip(Long msId, String content) {
    this.msId = msId;
    this.content = content;
  }

  public Long getMsId() {
    return msId;
  }

  public void setMsId(Long msId) {
    this.msId = msId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "MsShip{" +
        "msId=" + msId +
        ", content='" + content + '\'' +
        '}';
  }
}
