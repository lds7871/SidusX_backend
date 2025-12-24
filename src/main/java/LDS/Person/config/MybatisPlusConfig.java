package LDS.Person.config;

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
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        logger.info("✅ 初始化 MyBatis-Plus 拦截器");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        try {
            // 通过反射动态加载 PaginationInnerInterceptor
            Class<?> paginationClass = Class.forName(
                "com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor");
            
            // 获取 DbType 枚举类
            Class<?> dbTypeClass = Class.forName("com.baomidou.mybatisplus.annotation.DbType");
            Object postgreSqlType = dbTypeClass.getField("POSTGRE_SQL").get(null);
            
            // 通过反射创建 PaginationInnerInterceptor 实例
            Object pagination = paginationClass.getConstructor(dbTypeClass)
                .newInstance(postgreSqlType);
            
            logger.info("✅ PaginationInnerInterceptor 创建成功");
            
            // 通过反射调用 addInnerInterceptor 方法
            Class<?> innerInterceptorClass = Class.forName(
                "com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor");
            java.lang.reflect.Method addMethod = MybatisPlusInterceptor.class
                .getMethod("addInnerInterceptor", innerInterceptorClass);
            addMethod.invoke(interceptor, pagination);
            
            logger.info("✅ 分页拦截器配置成功");
            
        } catch (ClassNotFoundException e) {
            logger.warn("⚠️ MyBatis-Plus 分页拦截器类未找到，请检查依赖: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("❌ 分页拦截器配置失败: {}", e.getMessage());
        }
        
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
