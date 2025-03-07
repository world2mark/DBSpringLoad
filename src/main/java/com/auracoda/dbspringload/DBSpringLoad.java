package com.auracoda.dbspringload;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@RestController
public class DBSpringLoad {

	@Autowired
	private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(DBSpringLoad.class, args);
	}

	@GetMapping("test")
	public List<String> getTest() {
		return List.of("TEST path", "MZ!");
	};

	// @Bean
	// public HikariDataSource verticaDataSource() {
	// 	HikariConfig hikariConfig = new HikariConfig();

	// 	hikariConfig.setConnectionTimeout(env.getProperty("vertica.datasource.hikari.connectionTimeout", Long.class));

	// 	hikariConfig.setIdleTimeout(env.getProperty("vertica.datasource.hikari.idleTimeout", Long.class));

	// 	hikariConfig.setMaxLifetime(env.getProperty("vertica.datasource.hikari.maxLifetime", Long.class));

	// 	hikariConfig.setKeepaliveTime(env.getProperty("vertica.datasource.hikari.keepaliveTime", Long.class));

	// 	hikariConfig.setMaximumPoolSize(env.getProperty("vertica.datasource.hikari.maximumPoolSize", Integer.class));

	// 	hikariConfig.setMinimumIdle(env.getProperty("vertica.datasource.hikari.minimumIdle", Integer.class));

	// 	return new HikariDataSource(hikariConfig);

	// }
}
