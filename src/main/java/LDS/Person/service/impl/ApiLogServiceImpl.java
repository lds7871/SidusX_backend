package LDS.Person.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import LDS.Person.dto.request.ApiLogPageRequest;
import LDS.Person.entity.ApiLog;
import LDS.Person.repository.ApiLogMapper;
import LDS.Person.service.ApiLogService;
import org.springframework.stereotype.Service;

/**
 * 访问日志服务实现类
 */
@Service
public class ApiLogServiceImpl extends ServiceImpl<ApiLogMapper, ApiLog> implements ApiLogService {

    @Override
    public Page<ApiLog> getApiLogPage(ApiLogPageRequest request) {
        Page<ApiLog> page = new Page<>(request.getPageNum(), request.getPageSize());
        
        LambdaQueryWrapper<ApiLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (request.getStates() != null) {
            queryWrapper.eq(ApiLog::getStates, request.getStates());
        }
        
        // 时间段筛选
        if (request.getStartTime() != null) {
            queryWrapper.ge(ApiLog::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            queryWrapper.le(ApiLog::getCreateTime, request.getEndTime());
        }
        
        // 按时间倒序
        queryWrapper.orderByDesc(ApiLog::getCreateTime);
        
        return this.page(page, queryWrapper);
    }
}
