package LDS.Person;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;

/**
 * GHstart SpringBoot 启动应用类
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("LDS.Person.jmapper")
public class GHstartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GHstartApplication.class, args);
        System.out.println("GHstart 应用启动成功！");
        System.out.println("访问地址: http://localhost:8090");
    }

}
