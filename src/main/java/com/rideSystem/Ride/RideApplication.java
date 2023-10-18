package com.rideSystem.Ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RideApplication {

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(RideApplication.class);
		application.setAllowCircularReferences(Boolean.TRUE);


		application.run(args);
	}

}
