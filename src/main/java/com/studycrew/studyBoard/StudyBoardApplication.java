package com.studycrew.studyBoard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StudyBoardApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudyBoardApplication.class, args);
	}

}
