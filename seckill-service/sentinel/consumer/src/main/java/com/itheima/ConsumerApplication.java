package com.itheima;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.ConsumerApplication
 ****/
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.itheima.feign"})
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class,args);
    }
}
