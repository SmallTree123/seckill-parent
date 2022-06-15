package com.itheima.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;

/*****
 * @Author: itheima
 * @Project: seckill
 * @Description: com.itheima.config.GatewayConfiguration
 ****/
@Configuration
public class GatewayConfiguration {

    private final List<ViewResolver> viewResolvers;

    private final ServerCodecConfigurer serverCodecConfigurer;

    public GatewayConfiguration(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /****
     * 限流后异常处理
     * @return
     */
    //@Bean
    //@Order(Ordered.HIGHEST_PRECEDENCE)
    //public SentinelGatewayBlockExceptionHandler sentinelGatewayBlockExceptionHandler() {
    //    SentinelGatewayBlockExceptionHandler handler = new SentinelGatewayBlockExceptionHandler(viewResolvers, serverCodecConfigurer);
    //    return handler;
    //}

    /***
     * sentinel过滤器
     * @return
     */
    //@Bean
    //@Order(Ordered.HIGHEST_PRECEDENCE)
    //public GlobalFilter sentinelGatewayFilter() {
    //    return new SentinelGatewayFilter();
    //}
}
