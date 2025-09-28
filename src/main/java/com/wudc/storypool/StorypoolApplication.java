package com.wudc.storypool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StorypoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(StorypoolApplication.class, args);
	}

}
