package com.swadeshitech.prodhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProdhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProdhubApplication.class, args);
	}

}
