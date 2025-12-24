package LDS.Person.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis-Plus 配置类
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus 分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 直接使用反射加载分页插件（支持 3.5.x 版本）
        try {
            // 加载 PaginationInnerInterceptor 类
            Class<?> paginationInnerInterceptorClass = 
                Class.forName("com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor");
            
            // 获取构造函数 PaginationInnerInterceptor(DbType dbType)
            java.lang.reflect.Constructor<?> constructor = 
                paginationInnerInterceptorClass.getDeclaredConstructor(DbType.class);
            
            // 实例化为 PostgreSQL 类型
            Object paginationInstance = constructor.newInstance(DbType.POSTGRE_SQL);
            
            // 调用 setMaxLimit 方法
            java.lang.reflect.Method setMaxLimitMethod = 
                paginationInnerInterceptorClass.getMethod("setMaxLimit", Long.TYPE);
            setMaxLimitMethod.invoke(paginationInstance, 500L);
            
            // 添加到拦截器链
            java.lang.reflect.Method addInnerInterceptorMethod = 
                MybatisPlusInterceptor.class.getMethod("addInnerInterceptor", 
                    Class.forName("com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor"));
            addInnerInterceptorMethod.invoke(interceptor, paginationInstance);
            
        } catch (Exception e) {
            System.err.println("⚠️ 警告：无法加载 MyBatis-Plus 分页插件: " + e.getMessage());
            e.printStackTrace();
        }
        return interceptor;
    }
    /**
     * 配置 SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource, MybatisPlusInterceptor interceptor) throws Exception {
        MybatisSqlSessionFactoryBean sqlSessionFactory = new MybatisSqlSessionFactoryBean();
        sqlSessionFactory.setDataSource(dataSource);
        sqlSessionFactory.setPlugins(interceptor);
        sqlSessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml")
        );
        // 设置类型别名包
        sqlSessionFactory.setTypeAliasesPackage("LDS.Person.entity");
        return sqlSessionFactory.getObject();
    }

}
