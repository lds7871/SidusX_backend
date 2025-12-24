package LDS.Person.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import LDS.Person.dto.request.ApiLogPageRequest;
import LDS.Person.entity.ApiLog;

/**
 * 访问日志服务接口
 */
public interface ApiLogService extends IService<ApiLog> {
    
    /**
     * 分页查询访问日志
     * @param request 查询参数
     * @return 分页结果
     */
    Page<ApiLog> getApiLogPage(ApiLogPageRequest request);
}
