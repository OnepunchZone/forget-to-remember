package ru.sazon.forget_to_remember;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ForgetToRememberApplication {

	public static void main(String[] args) {
		SpringApplication.run(ForgetToRememberApplication.class, args);
	}

}
