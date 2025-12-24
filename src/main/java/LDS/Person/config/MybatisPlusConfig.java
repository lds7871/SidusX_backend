package LDS.Person.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置类
 * 配置分页插件以支持 PostgreSQL 数据库的分页查询
 */
@Configuration
public class MybatisPlusConfig {

    private static final Logger logger = LoggerFactory.getLogger(MybatisPlusConfig.class);

    /**
     * MyBatis-Plus 拦截器配置
     * 直接配置PostgreSQL分页拦截器，无需反射
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        logger.info("✅ 初始化 MyBatis-Plus 拦截器");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        
        return interceptor;
    }

    /**
     * 配置 SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MybatisPlusInterceptor interceptor) throws Exception {
        logger.info("✅ 初始化 SqlSessionFactory");
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setPlugins(interceptor);
        sqlSessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")
        );
        sqlSessionFactory.setTypeAliasesPackage("LDS.Person.entity");
        logger.info("✅ SqlSessionFactory 配置完成");
        return sqlSessionFactory.getObject();
    }
}
