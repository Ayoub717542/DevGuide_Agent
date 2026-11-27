package com.example.DevTutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevTutorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevTutorApplication.class, args);
	}

}
