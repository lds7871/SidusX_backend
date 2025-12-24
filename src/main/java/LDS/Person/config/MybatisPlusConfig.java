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
     * MyBatis-Plus 分页插件 - 使用反射兼容不同版本
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        logger.info("✅ 初始化 MyBatis-Plus 拦截器");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        try {
            // 加载分页拦截器类
            Class<?> paginationClass = Class.forName(
                "com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor");
            
            // 尝试用 DbType 参数构造
            Object pagination = null;
            try {
                java.lang.reflect.Constructor<?> constructor = 
                    paginationClass.getConstructor(DbType.class);
                pagination = constructor.newInstance(DbType.POSTGRE_SQL);
                logger.info("✅ 使用 DbType 参数创建 PaginationInnerInterceptor");
            } catch (NoSuchMethodException e) {
                // 如果没有该构造函数，尝试无参构造
                pagination = paginationClass.getDeclaredConstructor().newInstance();
                logger.info("✅ 使用无参构造创建 PaginationInnerInterceptor");
            }
            
            // 设置 MaxLimit
            try {
                java.lang.reflect.Method setMaxLimitMethod = 
                    paginationClass.getMethod("setMaxLimit", Long.TYPE);
                setMaxLimitMethod.invoke(pagination, 20L);
                logger.info("✅ 设置分页最大记录数: 20");
            } catch (NoSuchMethodException e) {
                logger.warn("⚠️ setMaxLimit 方法不存在，跳过设置");
            }
            
            // 添加到拦截器
            java.lang.reflect.Method addMethod = 
                MybatisPlusInterceptor.class.getMethod("addInnerInterceptor",
                    Class.forName("com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor"));
            addMethod.invoke(interceptor, pagination);
            
            logger.info("✅ 分页拦截器配置成功");
            
        } catch (Exception e) {
            logger.error("❌ 分页拦截器配置失败: {}", e.getMessage());
            e.printStackTrace();
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
