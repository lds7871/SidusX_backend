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
        // 尝试通过反射添加分页插件（兼容没有该类或类签名变化的情况）
        try {
            Class<?> paginationClass = Class.forName("com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor");
            Object paginationInstance = null;
            try {
                // 优先尝试带 DbType 的构造函数
                paginationInstance = paginationClass.getConstructor(com.baomidou.mybatisplus.annotation.DbType.class).newInstance(DbType.MYSQL);
            } catch (NoSuchMethodException ex) {
                // 回退到无参构造
                paginationInstance = paginationClass.getDeclaredConstructor().newInstance();
            }

            // 通过反射找到 addInnerInterceptor 方法并调用
            for (java.lang.reflect.Method m : MybatisPlusInterceptor.class.getMethods()) {
                if (m.getName().equals("addInnerInterceptor") && m.getParameterCount() == 1) {
                    m.invoke(interceptor, paginationInstance);
                    break;
                }
            }
        } catch (ClassNotFoundException ignored) {
            // mybatis-plus 版本不包含 PaginationInnerInterceptor，跳过分页插件
        } catch (Exception e) {
            // 任何反射相关异常都记录并忽略以保持应用可启动
            System.err.println("Failed to add PaginationInnerInterceptor via reflection: " + e.getMessage());
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
