package LDS.Person.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import LDS.Person.entity.ApiLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 访问日志 Mapper 接口
 */
@Mapper
public interface ApiLogMapper extends BaseMapper<ApiLog> {
}
