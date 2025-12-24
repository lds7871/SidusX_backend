package LDS.Person.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import LDS.Person.dto.request.ApiLogPageRequest;
import LDS.Person.entity.ApiLog;
import LDS.Person.repository.ApiLogMapper;
import LDS.Person.service.ApiLogService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 访问日志服务实现类
 * 提供日志分页查询、状态和时间段筛选功能
 */
@Service
public class ApiLogServiceImpl extends ServiceImpl<ApiLogMapper, ApiLog> implements ApiLogService {

    private static final Logger logger = LoggerFactory.getLogger(ApiLogServiceImpl.class);

    @Override
    public Page<ApiLog> getApiLogPage(ApiLogPageRequest request) {
        try {
            // 参数验证和默认值设置
            if (request == null) {
                request = new ApiLogPageRequest();
            }
            
            // 验证分页参数
            Integer pageNum = request.getPageNum();
            if (pageNum == null || pageNum <= 0) {
                pageNum = 1;
            }
            
            Integer pageSize = request.getPageSize();
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }
            
            // 限制每页最大数量为 100
            if (pageSize > 100) {
                pageSize = 100;
            }
            
            logger.debug("执行分页查询 - 页码: {}, 每页条数: {}", pageNum, pageSize);
            
            // 构建分页对象
            Page<ApiLog> page = new Page<>(pageNum, pageSize);
            
            // 构建查询条件
            LambdaQueryWrapper<ApiLog> queryWrapper = new LambdaQueryWrapper<>();
            
            // 状态筛选 - 精确匹配
            if (request.getStates() != null && request.getStates() > 0) {
                logger.debug("添加状态筛选条件: {}", request.getStates());
                queryWrapper.eq(ApiLog::getStates, request.getStates());
            }
            
            // 时间段筛选 - 开始时间 (大于等于)
            if (request.getStartTime() != null) {
                logger.debug("添加开始时间筛选条件: {}", request.getStartTime());
                queryWrapper.ge(ApiLog::getCreateTime, request.getStartTime());
            }
            
            // 时间段筛选 - 结束时间 (小于等于)
            if (request.getEndTime() != null) {
                logger.debug("添加结束时间筛选条件: {}", request.getEndTime());
                queryWrapper.le(ApiLog::getCreateTime, request.getEndTime());
            }
            
            // 按创建时间倒序排列（最新的在前）
            queryWrapper.orderByDesc(ApiLog::getCreateTime);
            
            // 执行分页查询
            logger.debug("执行数据库分页查询");
            Page<ApiLog> resultPage = this.baseMapper.selectPage(page, queryWrapper);
            
            logger.debug("查询完成 - 返回记录数: {}, 总记录数: {}, 总页数: {}", 
                resultPage.getRecords().size(), resultPage.getTotal(), resultPage.getPages());
            
            return resultPage;
            
        } catch (Exception e) {
            logger.error("分页查询失败: ", e);
            // 返回空分页结果
            return new Page<>(request == null ? 1 : request.getPageNum(), 
                             request == null ? 10 : request.getPageSize());
        }
    }
}
