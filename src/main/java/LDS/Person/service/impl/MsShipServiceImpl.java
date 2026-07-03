package LDS.Person.service.impl;

import LDS.Person.entity.MsShip;
import LDS.Person.repository.MsShipMapper;
import LDS.Person.service.MsShipService;
import LDS.Person.dto.request.MsShipCreateRequest;
import LDS.Person.dto.response.MsShipResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MS_SHIP 业务逻辑实现类
 */
@Service
public class MsShipServiceImpl implements MsShipService {

  private static final Logger log = LoggerFactory.getLogger(MsShipServiceImpl.class);

  private final MsShipMapper msShipMapper;

  public MsShipServiceImpl(MsShipMapper msShipMapper) {
    this.msShipMapper = msShipMapper;
  }

  @Override
  @Transactional
  public MsShipResponse createMsShip(MsShipCreateRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("请求不能为空");
    }

    String content = request.getContent();
    if (content == null || content.trim().isEmpty()) {
      throw new IllegalArgumentException("JSON 内容不能为空");
    }

    // 验证 JSON 格式（基础检查）
    if (!isValidJson(content.trim())) {
      throw new IllegalArgumentException("JSON 格式不正确");
    }

    try {
      MsShip msShip = new MsShip(content);
      int result = msShipMapper.insertMsShip(msShip);

      if (result > 0) {
        log.info("MS_SHIP 记录创建成功 - msId: {}", msShip.getMsId());
        return MsShipResponse.builder()
            .msId(msShip.getMsId())
            .content(msShip.getContent())
            .build();
      } else {
        throw new RuntimeException("插入失败");
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      log.error("MS_SHIP 记录创建失败", e);
      throw new RuntimeException("创建 MS_SHIP 记录失败: " + e.getMessage(), e);
    }
  }

  @Override
  public MsShipResponse getMsShipById(Long msId) {
    if (msId == null || msId <= 0) {
      throw new IllegalArgumentException("MS_ID 不能为空或无效");
    }

    try {
      MsShip msShip = msShipMapper.selectMsShipById(msId);

      if (msShip == null) {
        throw new IllegalArgumentException("MS_SHIP 记录不存在: " + msId);
      }

      log.info("MS_SHIP 记录查询成功 - msId: {}", msId);
      return MsShipResponse.builder()
          .msId(msShip.getMsId())
          .content(msShip.getContent())
          .build();
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      log.error("MS_SHIP 记录查询失败 - msId: {}", msId, e);
      throw new RuntimeException("查询 MS_SHIP 记录失败: " + e.getMessage(), e);
    }
  }

  /**
   * 简单的 JSON 格式验证
   */
  private boolean isValidJson(String content) {
    content = content.trim();
    return (content.startsWith("{") && content.endsWith("}")) ||
        (content.startsWith("[") && content.endsWith("]"));
  }
}
