package com.project.exam_point;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.project.exam_point.repository")
public class ExamPointApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExamPointApplication.class, args);
	}

}
