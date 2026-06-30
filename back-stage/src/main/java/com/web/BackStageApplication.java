package com.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.web.mapper")
@EnableScheduling
public class BackStageApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackStageApplication.class, args);
        System.out.println("启动成功");
    }

}
