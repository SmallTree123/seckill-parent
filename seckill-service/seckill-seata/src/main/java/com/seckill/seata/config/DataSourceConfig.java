package com.seckill.seata.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;

/*****
 * @Author: http://www.itheima.com
 * @Description: com.seckill.seata.config.DataSourceConfig
 ****/
@Configuration
public class DataSourceConfig {

    /****
     * 1.配置DataSource
     */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource dataSource(){
        return new DruidDataSource();
    }


    /***
     * 2.配置代理数据源
     */
    @Primary
    @Bean
    public DataSourceProxy dataSourceProxy(DruidDataSource dataSource){
        return new DataSourceProxy(dataSource);
    }


    /***
     * 3.持久层用的是MyBatis-plus
     *   SqlSessionFactory需要注入DataSource，将注入的DataSource换成代理数据源
     */
    @Bean
    @ConfigurationProperties(prefix = "mybatis-plus")
    public MybatisSqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource) throws IOException {
        // 这里用 MybatisSqlSessionFactoryBean 代替了 SqlSessionFactoryBean，否则 MyBatisPlus 不会生效
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(dataSource);
        return mybatisSqlSessionFactoryBean;
    }
}
