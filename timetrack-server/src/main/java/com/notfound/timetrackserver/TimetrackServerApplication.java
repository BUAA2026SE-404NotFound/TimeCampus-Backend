package com.notfound.timetrackserver;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.notfound")
@ConfigurationPropertiesScan(basePackages = "com.notfound")
@MapperScan("com.notfound.timetrackserver.mapper")
public class TimetrackServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimetrackServerApplication.class, args);
    }
}
