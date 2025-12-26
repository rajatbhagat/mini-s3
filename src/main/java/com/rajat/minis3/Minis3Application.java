package com.rajat.minis3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Minis3Application {

	public static void main(String[] args) {
		SpringApplication.run(Minis3Application.class, args);
	}

}
