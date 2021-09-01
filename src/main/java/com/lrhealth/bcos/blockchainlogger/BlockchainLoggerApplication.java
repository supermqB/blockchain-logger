package com.lrhealth.bcos.blockchainlogger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;

@ComponentScan(basePackages = {"cn.hongchain","com.lrhealth"})
@RestController
@SpringBootApplication
public class BlockchainLoggerApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(BlockchainLoggerApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(BlockchainLoggerApplication.class, args);
	}

}
