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
        // 参数验证
        if (request == null) {
            request = new ApiLogPageRequest();
        }
        
        Integer pageNum = request.getPageNum() != null && request.getPageNum() > 0 ? request.getPageNum() : 1;
        Integer pageSize = request.getPageSize() != null && request.getPageSize() > 0 ? request.getPageSize() : 10;
        
        // 构建分页对象
        Page<ApiLog> page = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<ApiLog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (request.getStates() != null && request.getStates() > 0) {
            queryWrapper.eq(ApiLog::getStates, request.getStates());
        }
        
        // 时间段筛选
        if (request.getStartTime() != null) {
            queryWrapper.ge(ApiLog::getCreateTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            queryWrapper.le(ApiLog::getCreateTime, request.getEndTime());
        }
        
        // 按时间倒序排列
        queryWrapper.orderByDesc(ApiLog::getCreateTime);
        
        // 直接调用 baseMapper.selectPage 执行分页查询
        return baseMapper.selectPage(page, queryWrapper);
    }
}
