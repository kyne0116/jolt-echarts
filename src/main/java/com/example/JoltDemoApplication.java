package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Jolt Demo 应用程序主类
 * 演示ECharts动态数据流架构
 */
@SpringBootApplication
public class JoltDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JoltDemoApplication.class, args);
        System.out.println("=== Jolt Demo 应用启动成功 ===");
        System.out.println("访问验证接口: http://localhost:8080/api/chart/validation/stacked-line");
        System.out.println("访问Mock数据: http://localhost:8080/api/chart/validation/mock-data");
        System.out.println("健康检查: http://localhost:8080/api/chart/validation/health");
    }
}
