package ru.sazon.forget_to_remember;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.h2.tools.Console;

import java.sql.SQLException;

@SpringBootApplication
@EnableScheduling
public class ForgetToRememberApplication {

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(ForgetToRememberApplication.class, args);
		Console.main(args);
	}

}
