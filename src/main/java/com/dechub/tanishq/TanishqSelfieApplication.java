package com.dechub.tanishq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//@SpringBootApplication(exclude = {ErrorMvcAutoConfiguration.class})
@SpringBootApplication
@EnableScheduling
public class TanishqSelfieApplication {

	public static void main(String[] args) {
		SpringApplication.run(TanishqSelfieApplication.class, args);
	}

}
