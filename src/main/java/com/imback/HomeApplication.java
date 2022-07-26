package com.imback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ServletComponentScan //扫描过滤器Filter
@Slf4j
@SpringBootApplication
@EnableTransactionManagement //开启事务注解的注释
public class HomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(HomeApplication.class,args);
        log.info("项目启动成功...");
    }
}
