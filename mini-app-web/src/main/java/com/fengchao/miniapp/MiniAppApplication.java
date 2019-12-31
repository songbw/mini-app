package com.fengchao.miniapp;

//import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
//import org.springframework.scheduling.annotation.EnableAsync;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(/*exclude={DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class}*/)
@EnableConfigurationProperties
//@EnableAsync
@MapperScan({"com.fengchao.miniapp.mapper"})
public class MiniAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniAppApplication.class, args);
	}

}
