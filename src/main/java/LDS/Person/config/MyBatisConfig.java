package LDS.Person.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 配置类
 * 用于配置 SqlSessionFactory 和 Mapper 扫描
 */
@Configuration
@MapperScan("LDS.Person.repository")
public class MyBatisConfig {

    /**
     * 配置 SqlSessionFactory
     * @param dataSource 数据源
     * @return SqlSessionFactory
     * @throws Exception 配置异常
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置 MyBatis 配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(true);
        configuration.setUseGeneratedKeys(true);
        configuration.setDefaultExecutorType(org.apache.ibatis.session.ExecutorType.REUSE);
        configuration.setDefaultStatementTimeout(30);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        
        factoryBean.setConfiguration(configuration);
        
        // 设置类型别名包
        factoryBean.setTypeAliasesPackage("LDS.Person.entity,LDS.Person.dto");
        
        // 设置 Mapper XML 文件位置(如果有的话)
        try {
            factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml")
            );
        } catch (Exception e) {
            // 如果没有 mapper XML 文件,忽略异常
            System.out.println("未找到 mapper XML 文件,将只使用注解方式");
        }
        
        return factoryBean.getObject();
    }
}
