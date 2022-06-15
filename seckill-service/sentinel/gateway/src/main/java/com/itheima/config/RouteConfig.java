package com.itheima.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.config.RouteConfig
 ****/
@Configuration
public class RouteConfig {

    /***
     * 路由配置
     * @param builder
     * @return
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("goods_consumer", p ->
                        p.path("/**")
                                .uri("lb://consumer")
                ).build();
    }
}
