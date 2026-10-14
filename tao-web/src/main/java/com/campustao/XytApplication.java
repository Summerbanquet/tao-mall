package com.campustao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campustao.**.mapper")   // 扫描六个业务模块的所有 Mapper
public class XytApplication {

    public static void main(String[] args) {
        SpringApplication.run(XytApplication.class, args);
    }
}