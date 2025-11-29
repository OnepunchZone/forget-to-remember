package ru.sazon.forget_to_remember;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

//import org.h2.tools.Console;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Scanner;

@SpringBootApplication
@EnableScheduling
public class ForgetToRememberApplication {

	public static void main(String[] args) throws SQLException {
		SpringApplication.run(ForgetToRememberApplication.class, args);
		//Console.main(args);
		Scanner scanner = new Scanner(System.in);
		System.out.println("=== Forget to Remember Console ===");
		while (true) {
			System.out.println("1. Добавить ДР\n2. Список ДР\n3. Создать шаблон\n4. Лайк шаблон\n0. Выход");
			String choice = scanner.nextLine();
			switch (choice) {
				case "1":
					System.out.print("Имя: ");
					String name = scanner.nextLine();
					System.out.print("Дата (YYYY-MM-DD): ");
					LocalDate date = LocalDate.parse(scanner.nextLine());
					System.out.print("Контакт: ");
					String contact = scanner.nextLine();
					// TODO: Интеграция с сервисом (currentUser из auth или hardcode)
					// birthdayService.createBirthday(new BirthdayDto(null, name, date, contact, 1L), user);
					System.out.println("ДР добавлен!");
					break;
				case "2":
					// TODO: List<BirthdayDto> birthdays = birthdayService.findByUser(user);
					// birthdays.forEach(b -> System.out.println(b.name() + " - " + b.date()));
					System.out.println("Список ДР...");
					break;
				case "3":
					System.out.print("Текст: ");
					String text = scanner.nextLine();
					System.out.print("isPublic (true/false): ");
					boolean isPublic = Boolean.parseBoolean(scanner.nextLine());
					// TODO: greetingService.createGreeting(new GreetingDto(null, text, null, isPublic, 0, 1L), user);
					System.out.println("Шаблон создан!");
					break;
				case "4":
					System.out.print("ID шаблона: ");
					Long id = Long.parseLong(scanner.nextLine());
					// TODO: greetingService.addLike(id);
					System.out.println("Лайк поставлен!");
					break;
				case "0":
					System.exit(0);
				default:
					System.out.println("Неверный выбор.");
			}
		}
	}

}
