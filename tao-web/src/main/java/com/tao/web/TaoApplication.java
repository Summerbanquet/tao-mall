package com.tao.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tao") // Bean扫描（@Service/@Component注解的业务类)
@MapperScan("com.tao.*.mapper") // mybatis mapper扫描（@Mapper注解的数据访问接口）
public class TaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaoApplication.class, args);
    }
}