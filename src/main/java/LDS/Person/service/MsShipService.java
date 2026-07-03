package LDS.Person.service;

import LDS.Person.dto.request.MsShipCreateRequest;
import LDS.Person.dto.response.MsShipResponse;

/**
 * MS_SHIP 业务服务接口
 */
public interface MsShipService {

  /**
   * 创建 MS_SHIP 记录
   *
   * @param request 创建请求
   * @return 创建成功的 MS_SHIP 响应
   */
  MsShipResponse createMsShip(MsShipCreateRequest request);

  /**
   * 根据 MS_ID 查询 MS_SHIP 记录
   *
   * @param msId MS_SHIP ID
   * @return MS_SHIP 响应
   */
  MsShipResponse getMsShipById(Long msId);
}
