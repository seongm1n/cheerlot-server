package academy.cheerlot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheerlotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheerlotApplication.class, args);
	}

}
