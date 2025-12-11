package com.campus.bookshare;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 校园二手书交易平台主类
 */
@SpringBootApplication
@MapperScan("com.campus.bookshare.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        System.out.println("=== 校园二手书交易平台启动成功 ===");
    }
}
