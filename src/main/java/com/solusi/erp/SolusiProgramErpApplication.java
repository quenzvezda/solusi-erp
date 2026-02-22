package com.solusi.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.solusi.erp")
@EnableJpaRepositories(basePackages = "com.solusi.erp")
public class SolusiProgramErpApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolusiProgramErpApplication.class, args);
	}

}
