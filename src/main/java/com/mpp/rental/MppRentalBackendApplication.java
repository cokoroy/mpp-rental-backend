package com.mpp.rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MppRentalBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MppRentalBackendApplication.class, args);
	}

}
