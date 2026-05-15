package com.notfound.timecampusserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.notfound")
@ConfigurationPropertiesScan(basePackages = "com.notfound")
@MapperScan("com.notfound.timecampusserver.mapper")
public class TimecampusServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimecampusServerApplication.class, args);
    }
}
