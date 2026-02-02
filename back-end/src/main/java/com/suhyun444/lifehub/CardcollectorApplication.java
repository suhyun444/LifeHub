package com.suhyun444.lifehub;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CardcollectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardcollectorApplication.class, args);
	}

}
